package TwinLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * ͬһʱ�����������̷߳��ʵĹ���������Դ������Ϊ2��
 * ���޸���Դ���Ķ���count�����й������Ķ��ơ�
 * 
 * @author tlm
 *
 */
public class TwinLock implements Lock {

	private static class Sync extends AbstractQueuedSynchronizer {

		private Sync(int count) {
			if (count > 0) {
				setState(count);
			} else {
				System.out.println("count must larger than 0.");
			}
		}

		@Override
		protected int tryAcquireShared(int arg) {
			for (;;) {
				int current = getState();
				int newstate = current - arg;
				if (current <= 0 || compareAndSetState(current, newstate)) {
					return newstate;
				}
			}
		}

		@Override
		protected boolean tryReleaseShared(int arg) {
			for (;;) {
				int current = getState();
				int newstate = current + arg;
				if (compareAndSetState(current, newstate)) {
					return true;
				}
			}
		}

	}

	private final Sync sync = new Sync(2);

	@Override
	public void lock() {
		sync.acquireShared(1);
	}

	@Override
	public void unlock() {
		sync.releaseShared(1);
	}
	
	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public Condition newCondition() {
		return null;
	}

	@Override
	public boolean tryLock() {
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return false;
	}
	
	/**
	 * ����ͳ�ƴ�ӡ�����ı�������ɾȥ
	 */
	public int count = 0;
	
	/**
	 * ��������TwinLock���main����
	 * @param args
	 */
	public static void main(String[] args) {
		final TwinLock lock = new TwinLock();
		final Object object = new Object();

		Runnable task = new Runnable() {
			@Override
			public void run() {
				while (true) {
					lock.lock();
					try {
						Thread.sleep(1000);
						System.out.println(Thread.currentThread().getName() + ":" + lock.count);
						
						//���д�ӡ������ͳ�ƣ���ɾȥ
						synchronized (object) {
							lock.count++;
						}
						
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						lock.unlock();
					}
				}
			}
		};

		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(task, "thread" + i);
			thread.setDaemon(true);
			thread.start();
		}

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("end:" + lock.count);
	}
}
