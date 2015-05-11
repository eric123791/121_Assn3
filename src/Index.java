import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Index {

	private static HashSet<String> stopWordsList = stopWords();
	static Integer numWords = 0;

	//termid:{[docid, frequency], [docid, frequency],....... }
	public static Map<String, Map<String, Integer>> allWordList = new HashMap<String, Map<String, Integer>>();
	//termid: frequency 
	public static Map<String, Integer> docWordList = new HashMap<String, Integer>();

	//termid: term
	private static Map<Integer, String> termIDToTerm = new HashMap<Integer, String>();
	//term: termid
	private static Map<String, Integer> termToTermID = new HashMap<String, Integer>();
	private static Integer termID  = 0;

	//docid: doc
	private static Map<Integer, String> docIDToDoc = new HashMap<Integer, String>();
	//doc: docid
	private static Map<String, Integer> docToDocID = new HashMap<String, Integer>();

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
		String path = input.getName();
		Scanner  sc = new Scanner (new FileReader(path));
		HashSet<String> result = new HashSet<String> ();

		while(sc.hasNext())
		{
			String line = sc.next();
			line = line.toLowerCase().replaceAll("[^a-z1-9']+", "");
			if(line.compareTo("")!=0)
				result.add(line);
		}
		sc.close();
		return result;
	}

	private static int termToID()  
	{
		return termID++; 
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

					if(n==1)
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
			writer = new PrintWriter("./frequency/" + fileName);
		} catch (FileNotFoundException e) {
			new File("./frequency").mkdirs();
		}
		
		try {
			writer = new PrintWriter("./frequency/" + fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//	System.out.println(fileName);
		
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

	private static void frequencyProcess()
	{
		File folder = new File("./doc/");
		File [] listOfFiles = folder.listFiles();

		int size = listOfFiles.length;
		for(int i = 0; i < size; ++i)
		{
			File file = listOfFiles[i];
			String filename = file.getName();
			Scanner wordSrc = null;

			byte[] jsonData = null;
			try {
				jsonData = Files.readAllBytes(Paths.get("./doc/" + filename));
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("READ FILE ERROR");
			}

			//create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();

			//convert json string to object
			Doc doc = null;
			try {
				doc = objectMapper.readValue(jsonData, Doc.class);
			} catch (JsonParseException e1) {
				e1.printStackTrace();
				System.out.println("ERROR AT JSON PARSE");
			} catch (JsonMappingException e1) {
				e1.printStackTrace();
				System.out.println("ERROR AT JSON PARSE");
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("ERROR AT JSON PARSE");
			}
			
			Integer  docid = Integer.parseInt(doc.getId().substring(0, doc.getId().indexOf(".")));
			docToDocID.put(filename, docid);
			docIDToDoc.put(docid, filename);	
	

			wordSrc = new Scanner(doc.getText());
			
			String text = "";
			while(wordSrc.hasNextLine())
			{
				text += wordSrc.nextLine();
			}
			wordSrc.close();
			processContent(text, filename);
		}
	}

	public static void startIndex()
	{
		frequencyProcess();
		//		System.out.println("docIDToDoc: " + docIDToDoc.toString());
		//		System.out.println("docToDocID: " + docToDocID.toString());
		//		System.out.println("termIDToTerm: " + termIDToTerm.toString());
		//		System.out.println("termToTermID: " + termToTermID.toString());

	}
	public static void main(String[] args) {
		startIndex();

		//number of document: is the size of the folder
		//number of unique words: is the size of the list of all doc word
		//output index to disk
		//time of the whole process

	}

}
