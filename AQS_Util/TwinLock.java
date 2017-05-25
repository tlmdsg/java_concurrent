package TwinLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * 同一时刻允许两个线程访问的共享锁，资源数定义为2。
 * 可修改资源数的定义count来进行共享锁的定制。
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
	 * 用来统计打印次数的变量，可删去
	 */
	public static int count = 0;
	
	/**
	 * 用来测试TwinLock类的main方法
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
						System.out.println(Thread.currentThread().getName() + ":" + count);
						
						//进行打印次数的统计，可删去
						synchronized (object) {
							count++;
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
