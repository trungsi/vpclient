/**
 * 
 */
package com.trungsi.vpclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;

import static com.trungsi.vpclient.VPClient.*;
import static com.trungsi.vpclient.utils.CollectionUtils.*;

/**
 * @author trungsi
 *
 */
public class VPClientAsync {

    private static final Logger LOG = Logger.getLogger(VPClientAsync.class);

    private final VPTaskWorker worker;
	private final Context context;
	private Sale selectedSale;
	
	private final ArrayList<VPTaskWorker> workers = new ArrayList<VPTaskWorker>();
	private WebDriverPool webDriverPool = new WebDriverPool();

	private Basket basket;

    private AtomicInteger articleCount;

    public static enum State {
		INIT, RUNNING, INTERRUPTED, TERMINATED
    }
	
	private State state;
	
	public VPClientAsync(Context context) {
		this.context = context;
		int poolSize = getPoolSize(context);
		worker = newVPTaskWorker(poolSize);
        basket = Basket.get(context.get(Context.USER));

        articleCount = new AtomicInteger();
        
		state = State.INIT;
	}
	
	private VPTaskWorker newVPTaskWorker(int poolSize) {
		VPTaskWorker worker = new VPTaskWorker(poolSize);
		workers.add(worker);
		LOG.info("new worker with size " + poolSize);
		return worker;
	}

	private int getPoolSize(Context context) {
		String poolSizeStr = context.get("poolSize");
		try {
			return Integer.parseInt(poolSizeStr);
		} catch (Exception e) {
			return 5;
		}
	}

	public void start(Sale selectedSale) {
		this.selectedSale = selectedSale;
		state = State.RUNNING;
		/*
		 * loadDriverTask
		 *     loadDriver
		 *     preloadDriverTask
		 *     findAllCategoriesTask
		 *         findAllCategories
		 *         loadAdditionalDriversTask (based on number of categories found)
		 *         findSubCategoriesInCategoryTask
		 *             findSubCategories
		 *             startNewTaskWorker (of 5 threads) -- new worker for each category 
		 *             findArticlesInSubCategoryTask (add to new task workers)
		 *                 findAllArticlesInSubCategory
		 *                 matchFilterArticle
		 *                 addArticleTask
		 *                     addArticle
		 *                     fireArticleAddedEvent
		 */
		addTasks(loadDriverTask());
	}
	
	void stop(long timeout) throws InterruptedException {
		this.worker.stop(timeout);
	}
	
	public void interrupt() {
		workers.forEach(worker -> worker.interrupt());
		
		state = State.INTERRUPTED;
	}
	
	public State getState() {
		if (state == State.RUNNING) {
			boolean isRunning = workers.stream().anyMatch(worker -> worker.isRunning());
			
			state = isRunning ? State.RUNNING : State.TERMINATED;
		}
		return state;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		Context context = VPGUI.loadContext(getVpHome(args));
		List<Sale> saleList = VPClient.getSalesListNew(VPClient.loadDriver(context));
		Optional<Sale> sale = saleList.stream()
			.filter(map -> map.getName().toLowerCase().contains(getSelectedSaleName(args)))
			.findFirst();
		
		long start = System.currentTimeMillis();
		VPClientAsync client = new VPClientAsync(context);
		client.start(sale.get());
		
		do {
			Thread.sleep(500);
		} while (client.getState() != VPClientAsync.State.TERMINATED);
		
		System.out.println(System.currentTimeMillis() - start + " : articles=" + client.articleCount.intValue() + ", added=" + client.basket.getBasketSize());
	}

	private static String getSelectedSaleName(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-s")) {
				if (i+1 >= args.length) {
					throw new RuntimeException("You must configure selected sale with -s option");
				}
				
				return args[i+1];
			}
		}
		return "lamarth";
	}

	private static String getVpHome(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-d")) {
				if (i+1 >= args.length) {
					throw new RuntimeException("You must configure vp home with -d option");
				}
				
				return args[i+1];
			}
		}
		
		return System.getProperty("user.home") + "/vente-privee";
	}

	private void addTasks(List<VPTask> tasks) {
		worker.addTasks(tasks);
	}

	private List<VPTask> loadDriverTask() {
		VPTask loadDriverTask = () -> {
            webDriverPool.add(loadDriver(context));
			
			List<VPTask> tasks = preloadDriverTasks();
			
			tasks.add(findAllCategoriesTask(selectedSale));
			
			return tasks;
		};
		
		return list(loadDriverTask);
	}

	protected List<VPTask> preloadDriverTasks() {
		return IntStream.range(0, 10)
			.mapToObj(i -> loadNewDriverAndAddToDriverQueueTask())
			.collect(Collectors.toList());
		
		/*ArrayList<VPTask> tasks = new ArrayList<VPTask>();
		for (int i = 1; i <= 10; i++) {
			tasks.add(loadNewDriverAndAddToDriverQueueTask());
		}
		
		return tasks;*/
	}

	private VPTask webDriverTask(WDTask wdTask) {
		return () -> {
	    	WebDriver webDriver = webDriverPool.getWebDriver();
	    	try {
	    		return wdTask.execute(webDriver);
	    	} finally {
	    		webDriverPool.releaseWebDriver(webDriver);
	    	}
		};
	}
	
	private VPTask findAllCategoriesTask(Sale selectedSale) {
		return webDriverTask((webDriver) -> {
			final List<Category> categories = 
					findAllCategories(webDriver, selectedSale, context);

            VPTask task = ()-> {
                // why 10 ?
                /*
                 * ArrayList<VPTask> tasks = new ArrayList<VPTask>();
                
                 for (int i = 10; i <= categories.size()*3; i++) {
                    tasks.add(loadNewDriverAndAddToDriverQueueTask());
                }
                
                for (Category category : categories) {
                    tasks.add(findArticlesInCategoryMessage(category));
                }

                return tasks;

                */
            	
                return Stream.concat(
                	IntStream.range(10, categories.size()*3+1)
                			.mapToObj(i -> loadNewDriverAndAddToDriverQueueTask()),
                		categories.stream()
                			.map(category -> findSubCategoriesInCategoryTask(category)))
                	.collect(Collectors.toList());
            };
            return list(task);
		});
	}

	protected VPTask loadNewDriverAndAddToDriverQueueTask() {
		return webDriverTask(webDriver -> {
			webDriverPool.add(cloneDriver(webDriver, context));
				
			return null;
		});
	}

	protected VPTask findSubCategoriesInCategoryTask(Category category) {
		return webDriverTask(webDriver -> {
			final List<SubCategory> subCategories = findSubCategories(webDriver, category, context);

            VPTask task = () -> {
                    // TODO DONE : no need WebDriver any more, how to release it ???                         
                    return list(() -> {
                        VPTaskWorker worker = newVPTaskWorker(5);
                        /*ArrayList<VPTask> tasks = new ArrayList<VPTask>();
                        for (SubCategory subCategory : subCategories) {
                            tasks.add(addArticlesInSubCategoryTask(subCategory));
                        }

                        worker.addTasks(tasks);*/
                        
                        worker.addTasks(
                        		subCategories.stream()
                        		.map(subCategory -> findArticlesInSubCategoryTask(subCategory))
                        		.collect(Collectors.toList()));

                        return null;
                    });

                
            };

            return list(task);
		});
	}

	protected VPTask findArticlesInSubCategoryTask(SubCategory subCategory) {
		return webDriverTask(webDriver -> {
			List<Article> articles = findAllArticlesInSubCategory(webDriver, subCategory, context);
			
			return list(() ->
				// XXX : be VERY cautious here. The trick to create a task which creates other tasks is to release WebDriver asap
				// So putting the method call which makes use of WebDriver inside the creating task is buggy.
				// Because WebDriver is passed to another thread which provokes concurrent issue
				//findAllArticlesInSubCategory(webDriver, subCategory, context)
					articles.stream()
					.filter(article -> matchFilterArticle(webDriver, article, context))
					.map(article -> addArticleTask(article))
					.collect(Collectors.toList()));
			
            });
	}

    protected VPTask addArticleTask(Article article) {
		return webDriverTask(webDriver -> {	
			articleCount.incrementAndGet();
			
			boolean added = addArticle(webDriver, article, context);

			if (added) {
				addArticleToBasket(article);
			}
			
			return null;
		});
	}

	private void addArticleToBasket(Article article) {
		basket.addArticle(article);
		
	}

	public void register(Object obj) {
		//eventBus.register(obj);
        basket.addUpdateListener(obj);
	}

	/*public List<Sale> getSalesList() {
		WebDriver driver = loadDriver(context);
		return VPClient.getSalesList(driver);
	}*/

	public List<Sale> getSalesListNew() {
		WebDriver driver = loadDriver(context);
		return VPClient.getSalesListNew(driver);
	}

	public int getBasketSize() {
        WebDriver driver = loadDriver(context);
        return VPClient.getBasketSize(driver);
    }

	public Sale getSelectedSale() {
		return selectedSale;
	}
}
