import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.io.*;

import org.apache.commons.codec.digest.DigestUtils;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	//helper for 2
	public static HashSet<String> urlList = new HashSet<String>();
	static int numUrl = 0;

	//helper for 3
	public static Map<String, Integer> subDomainList  = new HashMap<String, Integer>();
	static int numSubDomain = 0;

	//helper for 4
	public static int longestPageLength = 0;
	public static String longestPageUrl;

	//helper for 5
	public static Map<String, Integer> wordList = new HashMap<String, Integer>();
	static int numWords = 0;
	private static HashSet<String> stopWordsList = stopWords();


	private final static Pattern UltimateFILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g|ico" 
			+ "|ps|eps|tex|ppt|pptx|doc|docx|xls|xlsx|epub|names|data|dat|exe|msi"
			+ "|png|tiff?|mid|mp2|mp3|mp4|xml|wmf"    + "|wav|avi|mov|mpeg|ram|m4v|pdf|csv" 
			+ "|txt|cpp|c|h|cc|java|py|m|class|o|tmp" + "|perl|pl|vb|r|q|s|asm|rb|pas|bak|sh|awk|sed"
			+ "|rm|smil|wmv|swf|wma|zip|rar|gz|bz2|tar|jar))$");


	public static HashSet<String> tokenize(File input) throws Exception {
		// TODO Write body!
		String path = input.getName();
		Scanner  sc = new Scanner (new FileReader(path));
		HashSet<String> result = new HashSet<String> ();

		while(sc.hasNext())
		{
			String line = sc.next();
			line = line.toLowerCase().replaceAll("[^a-z1-9']+", "");
			result.add(line);
		}
		sc.close();
		return result;
	}
	private static HashSet<String> stopWords()
	{
		try{
			File file = new File("stopwordlist.txt");
			HashSet <String> result =  tokenize(file);
			return result;
		}catch(Exception e){
			System.out.println("STOPWORDS ERROR");
		}
		return null;
	}
	@Override
	public boolean shouldVisit(Page refPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		if (UltimateFILTERS.matcher(href).matches()) { 
			return false; 
		} if (!href.contains(".ics.uci.edu") && !href.contains("/ics.uci.edu")) {
			return false;
		}
		return followsPolicies(href);
	}

	/*
	 * Only accept the url if it follows the extended policies.
	 */
	private boolean followsPolicies(String href) {
		// TODO add extra policies here
		if (href.contains("?")) {
			return false;
		}
		if (href.contains("calendar.ics.uci.edu")) {
			return false;
		}
		// filter dynamic web page;
		if (href.contains("archive.ics.uci.edu/ml/datasets.html?")) {
			return false;
		}
		// filter machine learning dataset;
		if (href.contains("machine-learning-databases")) {
			return false;
		}
		// website that fails to response (trap);
		if (href.contains("fano.ics.uci.edu:80")) {
			return false;
		}
		if (href.contains("core7.ics.uci.edu:80")) {
			return false;
		}
		if (href.contains("www.ics.uci.edu/prospective/")) {
			return false;
		}
		return true;
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		//getting page date
		String url = page.getWebURL().getURL();
		String domain = page.getWebURL().getDomain();
		String subDomain = page.getWebURL().getSubDomain();
		String hashedUrl = getSHA256(url);

		//logger.info("URL: {}", url);


		//backup plan putting url and subdomain into repective txt file
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("url.txt", true)))) {
			out.println(url +  " " + hashedUrl);
		}catch (IOException e) {}

		try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("subdomain.txt", true)))) {
			out.println(subDomain+ "." +domain);
		}catch (IOException e)
		{}

		//getting data of the page
		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();


			if(longestPageLength < text.length())
			{
				longestPageLength = text.length();
				longestPageUrl = url;
				try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("longesturl.txt", true)))) {
					out.println(longestPageUrl + " " + longestPageLength);
				}catch (IOException e)
				{}
			}

			//backup plan
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter( "./data/" +hashedUrl +".txt", true)))) {
				out.println(text);
			}catch (IOException e)
			{}
		}

	}
	private String getSHA256(String s) {
		if (s.length() == 0) {
			return null;
		}
		return DigestUtils.sha256Hex(s);
	}

	/**
	 * This method is for testing purposes only. It does not need to be used
	 * to answer any of the questions in the assignment. However, it must
	 * function as specified so that your crawler can be verified programatically.
	 *
	 * This methods performs a crawl starting at the specified seed URL. Returns a
	 * collection containing all URLs visited during the crawl.
	 * @throws Exception 
	 */
	public static Collection<String> crawl(String seedURL) throws Exception {
		String crawlStorageFolder = "crawlStorageFolder";

		int numberOfCrawlers = 7;
		String userAgent = "UCI Inf141-CS121 crawler 49399981 39606094 83043652";
		//int timeOut = 1000*60*15;

		CrawlConfig config = new CrawlConfig();
		config.setUserAgentString(userAgent);

		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setPolitenessDelay(550);
		config.setResumableCrawling(true);
		//config.setMaxPagesToFetch(100);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		controller.addSeed(seedURL);
		controller.start(Crawler.class, numberOfCrawlers);

		System.out.println("finished");
		processEverything();
		System.out.println("URL: " + numUrl);
		System.out.println("Words: " + numWords);

		return urlList;

	}

	private static void processEverything() throws FileNotFoundException
	{
		//URL list
		Scanner urlSrc = new Scanner(new File("url.txt"));
		while(urlSrc.hasNextLine())
		{
			String url = urlSrc.nextLine();
			++numUrl;
			urlList.add(url);
		}
		urlSrc.close();
		//Commonword List
		File folder = new File("./data/");
		File [] listOfFiles = folder.listFiles();
		
		int size = listOfFiles.length;
		for(int i = 0; i < size; ++i)
		{
			File file = listOfFiles[i];
			Scanner wordSrc = new Scanner(new File("./data/" + file.getName()));
			while(wordSrc.hasNextLine())
			{
				String text = wordSrc.nextLine();
				processWord(text);
			}
			wordSrc.close();
		}
			
	}
	public static void printWords() throws Exception
	{
		wordListSort();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Answers.txt", true))) ;
		PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter("CommonWords.txt", true))) ;
		for(Map.Entry<String,Integer> entry : wordList.entrySet())
		{
			String key = entry.getKey();
			Integer value = entry.getValue();
			out.println(key+" : "+value);
			out2.println(key+" : "+value);
		}
		out.close();
		out2.close();
	}

	public static void printSubDomain() throws Exception
	{
		subDomainListSort();
		PrintWriter writer = new PrintWriter("Subdomains.txt");
		for(Map.Entry<String,Integer> entry : subDomainList.entrySet())
		{
			String key = entry.getKey();
			Integer value = entry.getValue();
			writer.println(key+" : "+value);
		}
		writer.close();
	}

	private static void processWord(String words)
	{
		Scanner  sc = new Scanner (words);
		while(sc.hasNext())
		{
			String word = sc.next();
			word = word.toLowerCase().replaceAll("[^a-z0-9']+", "");
			if(!stopWordsList.contains(word))
			{
				if(word.compareTo("") != 0)
				{
					Integer n = wordList.get(word);
					n = (n == null) ? 1: ++n;
					wordList.put(word, n);
					if(n==1)
						++numWords;
				}
			}
		}
		sc.close();
	}

	public static void subDomainListSort()
	{
		subDomainList = new TreeMap<String, Integer> (subDomainList);
	}

	public static void wordListSort()
	{
		List<Map.Entry<String, Integer>> list =
				new LinkedList<Map.Entry<String, Integer>>(wordList.entrySet());

		// Defined Custom Comparator here
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				if((o1.getValue()).compareTo(o2.getValue()) == 0)
				{
					return (o1.getKey()).compareTo(o2.getKey())*-1;
				}
				return (o1.getValue()).compareTo(o2.getValue())*-1;
			}
		});

		wordList = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			wordList.put(entry.getKey(), entry.getValue());
		}
	}

	public static void main(String[] args) throws Exception
	{
		crawl("http://www.ics.uci.edu/");
	}

}
