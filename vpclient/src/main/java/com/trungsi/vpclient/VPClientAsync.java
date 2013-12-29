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
import org.apache.log4j.Logger;

import org.openqa.selenium.WebDriver;

import com.google.common.eventbus.EventBus;

import static com.trungsi.vpclient.VPClient.*;
import static com.trungsi.vpclient.utils.CollectionUtils.*;

/**
 * @author trungsi
 *
 */
public class VPClientAsync {

    private static final Logger LOG = Logger.getLogger(VPClientAsync.class);

    private final VPTaskWorker worker;
	final Map<String, String> context;
	
	private final ArrayList<VPTaskWorker> workers = new ArrayList<VPTaskWorker>();
	private WebDriverProvider driverProvider = new WebDriverProvider();

	private final EventBus eventBus = new EventBus();
	
	public static enum State {
		INIT, RUNNING, INTERRUPTED, TERMINATED
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
		LOG.info("new worker with size " + poolSize);
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
		addTasks(loadDriverTask());
	}
	
	void stop(long timeout) throws InterruptedException {
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
				if (worker.isRunning()) {
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

	private List<VPTask> loadDriverTask() {
		VPTask loadDriverTask = new VPTask() {
			public List<VPTask> execute() {
                driverProvider.add(loadDriver(context));
				
				List<VPTask> tasks = preloadDriverTasks();
				
				tasks.addAll(findAllCategoriesTask());
				
				return tasks;
			}
		};
		
		return list(loadDriverTask);
	}

	protected List<VPTask> preloadDriverTasks() {
		ArrayList<VPTask> tasks = new ArrayList<VPTask>();
		for (int i = 1; i <= 10; i++) {
			tasks.add(loadNewDriverAndAddToDriverQueueTask());
		}
		
		return tasks;
	}

	protected List<VPTask> findAllCategoriesTask() {
		VPTask message = new WDTask(driverProvider) {
			public List<VPTask> execute(WebDriver webDriver) {

				final List<Map<String, String>> categories = findAllCategories(webDriver, context);

                VPTask task = new VPTask() {
                    @Override
                    public List<VPTask> execute() {
                        ArrayList<VPTask> tasks = new ArrayList<VPTask>();
                        // why 10 ?
                        for (int i = 10; i <= categories.size()*3; i++) {
                            tasks.add(loadNewDriverAndAddToDriverQueueTask());
                        }

                        for (Map<String, String> category : categories) {
                            tasks.add(findArticlesInCategoryMessage(category));
                        }

                        return tasks;
                    }
                };
                return list(task);
			}

			
		};
		
		return list(message);
		
	}

	protected VPTask loadNewDriverAndAddToDriverQueueTask() {
		return new WDTask(driverProvider) {
			//@Override
			public List<VPTask> execute(WebDriver webDriver) {

                driverProvider.add(cloneDriver(webDriver, context));
				
				return null;
			}
		};
	}

	protected VPTask findArticlesInCategoryMessage(
			final Map<String, String> category) {
		return new WDTask(driverProvider) {
			public List<VPTask> execute(WebDriver webDriver) {
				List<Map<String, String>> subCategories = findSubCategories(webDriver, category, context);


                // TODO : no need WebDriver any more, how to release it ???
				VPTask task = null;
				if (subCategories.isEmpty()) { // no categories
					task = addArticlesInSubCategoryTask(category, new HashMap<String, String>());
				} else {
					final List<Map<String, String>> finalSubCategories = subCategories;
					task = new VPTask() {
						//@Override
						public List<VPTask> execute() {
							VPTaskWorker worker = newVPTaskWorker(5);
							ArrayList<VPTask> tasks = new ArrayList<VPTask>();
							for (Map<String, String> subCategory : finalSubCategories) {
								tasks.add(addArticlesInSubCategoryTask(category, subCategory));
							}
							
							worker.addTasks(tasks);
							
							return null;
						}
					};
				}
				
				
				return list(task);
			}
		};
	}

	protected VPTask addArticlesInSubCategoryTask(
            final Map<String, String> category,
            final Map<String, String> subCategory) {
		return new WDTask(driverProvider) {
			public List<VPTask> execute(WebDriver webDriver) {

				List<Map<String, String>> articleElems = findAllArticlesInSubCategory(webDriver, category, subCategory, context);

                // TODO : no need WebDriver any more, how to release it ???
				ArrayList<VPTask> tasks = new ArrayList<VPTask>();
				for (Map<String, String> articleElem : articleElems) {
					tasks.add(addArticleTask(category, subCategory, articleElem));
				}
				
				return tasks;
			}
		};
	}

	protected VPTask addArticleTask(
			final Map<String, String> category, final Map<String, String> subCategory,
			final Map<String, String> article) {
		return new WDTask(driverProvider) {
			public List<VPTask> execute(WebDriver webDriver) {

				boolean added = addArticle(webDriver, category, subCategory, article, context);

				if (added) {
					addAddArticleEvent(category, subCategory, article);
				}
				
				return null;
			}
		};
	}

	protected void addAddArticleEvent(Map<String, String> category,
			Map<String, String> subCategory, Map<String, String> article) {
		String text = category.get("name") + "|" + subCategory.get("name") + article.get("name");
		eventBus.post(new AddArticleEvent(text));
		
	}

	public void register(Object obj) {
		eventBus.register(obj);
	}

	public List<Map<String, String>> getSalesList() {
		WebDriver driver = loadDriver(context);
		return VPClient.getSalesList(driver);
	}
	
}