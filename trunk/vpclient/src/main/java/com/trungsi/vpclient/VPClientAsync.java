/**
 * 
 */
package com.trungsi.vpclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import static com.trungsi.vpclient.VPClient.*;

/**
 * @author trungsi
 *
 */
public class VPClientAsync {

	VPTaskWorker worker;
	Map<String, String> context;
	
	ArrayList<VPTaskWorker> workers = new ArrayList<VPTaskWorker>();
	
	public static enum State {
		INIT, RUNNING, INTERRUPTED, TERMINATED;
	}
	
	private State state;
	
	public VPClientAsync(Map<String, String> context) {
		this.context = context;
		int poolSize = getPoolSize(context);
		worker = newVPTaskWorker(poolSize);
		state = State.INIT;
	}
	
	private VPTaskWorker newVPTaskWorker(int poolSize) {
		VPTaskWorker worker = new VPTaskWorker(poolSize);
		workers.add(worker);
		
		return worker;
	}

	private int getPoolSize(Map<String, String> context) {
		String poolSizeStr = context.get("poolSize");
		try {
			return Integer.parseInt(poolSizeStr);
		} catch (Exception e) {
			return 5;
		}
	}

	public void start() {
		state = State.RUNNING;
		addTasks(loadDriverTask(context));
	}
	
	public void stop(long timeout) throws InterruptedException {
		this.worker.stop(timeout);
	}
	
	public void interrupt() {
		for (VPTaskWorker worker : workers) {
			worker.interrupt();
		}
		
		state = State.INTERRUPTED;
	}
	
	public State getState() {
		if (state == State.RUNNING) {
			for (VPTaskWorker worker : workers) {
				if (!worker.isStopped()) {
					return state;
				}
			}
			state = State.TERMINATED;
		}
		return state;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		Map<String, String> context = loadContext();
		VPClientAsync client = new VPClientAsync(context);
		client.start();
		
		Thread.sleep(1500000);
		client.stop(10000);
	}

	private static Map<String, String> loadContext() throws Exception {
		Properties props = new Properties();
		props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));

		HashMap<String, String> context = new HashMap<String, String>();
		for (Entry<?, ?> entry : props.entrySet()) {
			context.put(entry.getKey().toString(), entry.getValue().toString());
		}
		return context;
	}

	private void addTasks(List<VPTask> tasks) {
		worker.addTasks(tasks);
	}

	private List<VPTask> loadDriverTask(final Map<String, String> context) {
		VPTask loadDriverMessage = new VPTask() {
			public List<VPTask> execute() {
				BlockingQueue<WebDriver> driverQueue = new LinkedBlockingQueue<WebDriver>();
				driverQueue.add(loadDriver(context));
				
				List<VPTask> tasks = preloadDriverTasks(driverQueue, context);
				
				tasks.addAll(findAllCategoriesTask(driverQueue, context));
				
				return tasks;
			}
		};
		
		return list(loadDriverMessage);
	}

	protected List<VPTask> preloadDriverTasks(
			BlockingQueue<WebDriver> driverQueue, Map<String, String> context) {
		ArrayList<VPTask> tasks = new ArrayList<VPTask>();
		for (int i = 1; i <= 10; i++) {
			tasks.add(loadNewDriverAndAddToDriverQueueTask(driverQueue, context));
		}
		
		return tasks;
	}

	protected List<VPTask> findAllCategoriesTask(final BlockingQueue<WebDriver> driverQueue,
			final Map<String, String> context) {
		VPTask message = new VPTask() {
			public List<VPTask> execute() {
				WebDriver driver = removeFromQueue(driverQueue);
				List<Map<String, String>> categories = findAllCategories(driver, context);
				driverQueue.add(driver);
				
				ArrayList<VPTask> tasks = new ArrayList<VPTask>();
				
				for (int i = 10; i <= categories.size()*2; i++) {
					tasks.add(loadNewDriverAndAddToDriverQueueTask(driverQueue, context));
				}
				
				for (Map<String, String> category : categories) {
					tasks.add(findArticlesInCategoryMessage(driverQueue, category, context));
				}
				
				return tasks;
			}

			
		};
		
		return list(message);
		
	}

	protected VPTask loadNewDriverAndAddToDriverQueueTask(
			final BlockingQueue<WebDriver> driverQueue,
			final Map<String, String> context) {
		return new VPTask() {
			//@Override
			public List<VPTask> execute() {
				WebDriver otherDriver = driverQueue.peek();
				
				driverQueue.add(cloneDriver(otherDriver, context));
				
				return null;
			}
		};
	}

	protected VPTask findArticlesInCategoryMessage(
			final BlockingQueue<WebDriver> driverQueue,
			final Map<String, String> category, final Map<String, String> context) {
		return new VPTask() {
			public List<VPTask> execute() {
				WebDriver driver = removeFromQueue(driverQueue);
				final List<Map<String, String>> subCategories = findSubCategories(driver, category, context);
				driverQueue.add(driver);
				
				VPTask task = new VPTask() {
					//@Override
					public List<VPTask> execute() {
						VPTaskWorker worker = newVPTaskWorker(2);
						ArrayList<VPTask> tasks = new ArrayList<VPTask>();
						for (Map<String, String> subCategory : subCategories) {
							tasks.add(addArticlesInSubCategoryTask(driverQueue, category, subCategory, context));
						}
						
						worker.addTasks(tasks);
						
						return null;
					}
				};
				
				
				return list(task);
			}
		};
	}

	protected static VPTask addArticlesInSubCategoryTask(
			final BlockingQueue<WebDriver> driverQueue, final Map<String, String> category,
			final Map<String, String> subCategory, 
			final Map<String, String> context) {
		
		return new VPTask() {
			public List<VPTask> execute() {
				WebDriver driver = removeFromQueue(driverQueue);
				List<Map<String, String>> articleElems = findAllArticlesInSubCategory(driver, category, subCategory);
				driverQueue.add(driver);
				
				ArrayList<VPTask> tasks = new ArrayList<VPTask>();
				for (Map<String, String> articleElem : articleElems) {
					tasks.add(addArticleTask(driverQueue, category, subCategory, articleElem, context));
				}
				
				return tasks;
			}
		};
	}

	protected static VPTask addArticleTask(
			final BlockingQueue<WebDriver> driverQueue,
			final Map<String, String> category, final Map<String, String> subCategory,
			final Map<String, String> article, 
			final Map<String, String> context) {
		return new VPTask() {
			public List<VPTask> execute() {
				WebDriver driver = removeFromQueue(driverQueue);
				addArticle(driver, category, subCategory, article, context);
				driverQueue.add(driver);
				
				return null;
			}
		};
	}

	protected static WebDriver removeFromQueue(
			BlockingQueue<WebDriver> driverQueue) {
		try {
			return driverQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}