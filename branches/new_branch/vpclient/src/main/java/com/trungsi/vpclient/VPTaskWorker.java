/**
 * 
 */
package com.trungsi.vpclient;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author trungsi
 *
 */
public class VPTaskWorker {

	private ExecutorService service;
	
	private AtomicInteger size;
	private AtomicInteger executings;
	
	private Thread monitoringThread;
	public VPTaskWorker(int poolSize) {
		this.service = Executors.newFixedThreadPool(poolSize);
		size = new AtomicInteger();
		executings = new AtomicInteger();
		
		monitoringThread = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
					while (size.get() > 0 || executings.get() > 0) {
						Thread.sleep(5000);
					}
					
					VPTaskWorker.this.stop(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		monitoringThread.start();
	}
	
	public void addTasks(final List<VPTask> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			//System.out.println("No more message");
			return;
		}
		
		for (final VPTask task : tasks) {
			size.incrementAndGet();
			service.submit(new Callable<Void>() {
				//@Override
				public Void call() throws Exception {
					size.decrementAndGet();
					executings.incrementAndGet();
					try {
						addTasks(task.execute());
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						executings.decrementAndGet();
					}
					return null;
				}
			});
		}
	}
	
	public void stop(long timeout) throws InterruptedException {
		service.shutdown();
		service.awaitTermination(timeout, TimeUnit.MILLISECONDS);
	}
	
	public boolean isStopped() {
		return service.isTerminated();
	}
	
	public void interrupt() {
		service.shutdownNow();
	}
}
