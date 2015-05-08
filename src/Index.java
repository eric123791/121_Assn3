import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;


public class Index {

	private static HashSet<String> stopWordsList = stopWords();
	static Integer numWords = 0;
	
	public static Map<String, Integer> allWordList = new HashMap<String, Integer>();
	public static Map<String, Integer> docWordList = new HashMap<String, Integer>();
	
	private static Map<Integer, String> termIDToTerm = new HashMap<Integer, String>();
	private static Map<String, Integer> termToTermID = new HashMap<String, Integer>();
	private static Integer termID  = 0;
	
	private static Map<Integer, String> docIDToDoc = new HashMap<Integer, String>();
	private static Map<String, Integer> docToDocID = new HashMap<String, Integer>();
	private static Integer docID = 0;
	
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
	
	private static int termToID()  
	{
		return termID++; 
	}
	
	private static int docToID()
	{
		return docID++;
	}
	
	//get the termid: frequency for each doc and stored in ./index/filename
	private static void processContent(String words, String fileName)
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
					Integer n = docWordList.get(word);
					n = (n == null) ? 1: ++n;
					docWordList.put(word, n);
					
					Integer m = allWordList.get(word);
					m = (m == null) ? 1: ++n;
					allWordList.put(word, n);
					
					if(m==1)
					{
						++numWords;
						int id = termToID();
						termToTermID.put(word, id);
						termIDToTerm.put(id, word);	
					}
				}
			}
		}
		sc.close();
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("./index/" + fileName + ".txt");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR: processContent()" + "./index/" + fileName + ".txt" );
		}
	//	System.out.println(fileName);
		writer.println(docToDocID.get(fileName));
		for(Map.Entry<String,Integer> entry : docWordList.entrySet())
		{
			String key = entry.getKey();
			Integer id = termToTermID.get(key);
			Integer value = entry.getValue();
			writer.println(id+" : "+value);
			//System.out.println(id+" : "+value);
		}
		writer.close();
		docWordList = new HashMap<String, Integer>();
	}
	
	private static void processFile()
	{
		File folder = new File("./doc/");
		File [] listOfFiles = folder.listFiles();
		
		int size = listOfFiles.length;
		for(int i = 0; i < size; ++i)
		{
			File file = listOfFiles[i];
			String filename = file.getName();
			Scanner wordSrc = null;
			int docID = docToID();
			docToDocID.put(filename, docID);
			docIDToDoc.put(docID, filename);	
			
			try {
				wordSrc = new Scanner(new File("./doc/" + filename ));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("ERROR: processFile()" + "./doc/" + filename );
			}
			
			String text = "";
			while(wordSrc.hasNextLine())
			{
				text += wordSrc.nextLine();
			}
			processContent(text, filename);
			wordSrc.close();
			
			
		}
	}

	public static void startIndex()
	{
		processFile();
//		System.out.println("docIDToDoc: " + docIDToDoc.toString());
//		System.out.println("docToDocID: " + docToDocID.toString());
//		System.out.println("termIDToTerm: " + termIDToTerm.toString());
//		System.out.println("termToTermID: " + termToTermID.toString());
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		startIndex();
		
		//number of document is the size of the folder
		//number of unique words is the size of the list of all doc word
		//output index to disk
		//time of the whole process

	}

}
