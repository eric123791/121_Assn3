import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Sorting {
	private static ArrayList<String> termList = new ArrayList<String>();
	private static Map<Integer, Double> resultMap = new HashMap<Integer, Double>();
	private static ArrayList<Map.Entry<Integer, Double>> resultList = new ArrayList<Map.Entry<Integer, Double>>(); 
	
	//termid:{[docid, frequency], [docid, frequency],....... }
	public static Map<String, Map<String, Integer>> allWordList = new HashMap<String, Map<String, Integer>>();
	
	// termid 2 docid:TFIDF
	public static Map<Integer, Map<Integer, Double>> termid2docidNTFIDF = new HashMap<Integer, Map<Integer, Double>> ();
	
	//docid: number of term
	public static Map<String, Integer> docTerms = new HashMap<String, Integer> ();

	//termid2term  &&  term2termid
	private static Map<Integer, String> termIDToTerm = new HashMap<Integer, String>();
	private static Map<String, Integer> termToTermID = new HashMap<String, Integer>();

	//docid2doc  &&  doc2docid
	private static Map<Integer, String> docIDToDoc = new HashMap<Integer, String>();
	private static Map<String, Integer> docToDocID = new HashMap<String, Integer>();

	//docid 2 url
	public static Map<Integer, String> urlList = new HashMap<Integer, String>();
	
	
	
	public static int restore() {		
		int result = -1;
		// restore allWordList;
		System.out.println("Restoring allWordList...");
		result = restore_allWordList();
		System.out.println((result == 1) ? "Restoring allWordList successful." : "Fail to restore allWordList.");
		
		// restore termid2docidNTFIDF;
		System.out.println("\nRestoring termid2docidNTFIDF...");
		result = restore_termid2docidNTFIDF();
		System.out.println((result == 1) ? "Restoring termid2docidNTFIDF successful." : "Fail to restore termid2docidNTFIDF.");	
				
		// restore docTerms;
		System.out.println("\nRestoring docTerms...");
		result = restore_docTerms();
		System.out.println((result == 1) ? "Restoring docTerms successful." : "Fail to restore docTerms.");
		
		// restore termIDToTerm;
		System.out.println("\nRestoring termIDToTerm...");
		result = restore_termIDToTerm();
		System.out.println((result == 1) ? "Restoring termIDToTerm successful." : "Fail to restore termIDToTerm.");
		
		// restore termToTermID;
		System.out.println("\nRestoring termToTermID...");
		result = restore_termToTermID();
		System.out.println((result == 1) ? "Restoring termToTermID successful." : "Fail to restore termToTermID.");
		
		// restore docIDToDoc;
		System.out.println("\nRestoring docIDToDoc...");
		result = restore_docIDToDoc();
		System.out.println((result == 1) ? "Restoring docIDToDoc successful." : "Fail to restore docIDToDoc.");
		
		// restore docToDocID;
		System.out.println("\nRestoring docToDocID...");
		result = restore_docToDocID();
		System.out.println((result == 1) ? "Restoring docToDocID successful." : "Fail to restore docToDocID.");
		
		// restore urlList;
		System.out.println("\nRestoring urlList...");
		result = restore_urlList();
		System.out.println((result == 1) ? "Restoring urlList successful." : "Fail to restore urlList.");
		
		return 1;
	}
	
	public static ArrayList<String> getFinalResult(ArrayList<String> termList) {
		getTerms(termList);
		executing();
		return getResult();
	}
	
	public static void getTerms(ArrayList<String> tl) {
		termList.clear();
		for (String term : tl) {
			termList.add(term.trim());
		}
	}
	
	public static int executing() {
		resultMap.clear();
		for (String term : termList) {
			if (!termToTermID.containsKey(term)){
				continue;
			}
			int key = termToTermID.get(term);
			if (!termid2docidNTFIDF.containsKey(key)) {
				continue;
			}
			Map<Integer, Double> tempMap = termid2docidNTFIDF.get(key);
			for (int i : tempMap.keySet()) {
				Double rawScore = (resultMap.containsKey(i) ? resultMap.get(i) : 0);
				resultMap.put(i, tempMap.get(i) + rawScore);
			}
		}
		
		// wtf();
		for (int j : resultMap.keySet()) {
			Double realScore = (Math.log10((double)j) * 0.3 + resultMap.get(j) * 0.7) / 10;
			resultMap.put(j, realScore);
		}
		
		// sort the result and add to resultList;
		resultMapSort();
		
		return 1;
	}
	
	public static ArrayList<String> getResult() {
		ArrayList<String> toResult = new ArrayList<String>();
		int count = 10;
		String s;
		for (Map.Entry<Integer, Double> entry : resultList) {
			if (count < 1) {
				break;
			}
			int docID = entry.getKey();
			s = docIDToDoc.get(docID) + "\t" + Double.toString(entry.getValue()) + "\t" + urlList.get(docID);
			toResult.add(s);
			count--;
		}
		return toResult;
	}
	
	// RESTORING FUNCTION
	//================================================================================================================
	public static int restore_allWordList() {
		String path = "./mapping/allwordlist.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.split(":");
				String termid = lineInfo[0];
				String[] insideMapInfo = lineInfo[1].replaceAll("([{}])", "").split(",");
				Map<String, Integer> insideMap = new HashMap<String, Integer>();
				for (String s : insideMapInfo) {
					String[] insideMaplineInfo = s.trim().split("=");
					insideMap.put(insideMaplineInfo[0].trim(), Integer.parseInt(insideMaplineInfo[1].trim()));
				}
				allWordList.put(termid, insideMap);
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public static int restore_docTerms() {
		String path = "./mapping/docterms.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.trim().split(":");
				docTerms.put(lineInfo[0].trim(), Integer.parseInt(lineInfo[1].trim()));
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public static int restore_termIDToTerm() {
		String path = "./mapping/termidtoterm.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.trim().split(":");
				termIDToTerm.put(Integer.parseInt(lineInfo[0].trim()), lineInfo[1].trim());
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public static int restore_termToTermID() {
		String path = "./mapping/termtotermid.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.trim().split(":");
				termToTermID.put(lineInfo[0].trim(), Integer.parseInt(lineInfo[1].trim()));
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public static int restore_docIDToDoc() {
		String path = "./mapping/docidtodoc.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.trim().split(":");
				docIDToDoc.put(Integer.parseInt(lineInfo[0].trim()), lineInfo[1].trim());
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public static int restore_docToDocID() {
		String path = "./mapping/doctodocid.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.trim().split(":");
				docToDocID.put(lineInfo[0].trim(), Integer.parseInt(lineInfo[1].trim()));
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public static int restore_termid2docidNTFIDF() {
		String path = "./tfidf.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.trim().split(":");
				int termid = Integer.parseInt(lineInfo[0].trim());
				String[] insideMapInfo = lineInfo[1].trim().replaceAll("([{}])", "").split(",");
				Map<Integer, Double> insideMap = new HashMap<Integer, Double>();
				for (String s : insideMapInfo) {
					String[] insideMaplineInfo = s.trim().split("=");
					insideMap.put(Integer.parseInt(insideMaplineInfo[0].trim()), Double.parseDouble(insideMaplineInfo[1].trim()));
				}
				termid2docidNTFIDF.put(termid, insideMap);
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	
	public static int restore_urlList() {
		String path = "./mapping/urllist.txt";
		Scanner sc = null;
		try {
			sc = new Scanner (new FileReader(path));
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] lineInfo = line.trim().split(":", 2);
				urlList.put(Integer.parseInt(lineInfo[0].trim()), lineInfo[1].trim());
			}
			sc.close();
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
		return 1;
	}
	//================================================================================================================
	// END RESTORING FUNCTION

	
	// RESULTMAP SORT
	//================================================================================================================
	private static void resultMapSort() {
		resultList.addAll(resultMap.entrySet());
		
		// Define resultMapComparator;
		Collections.sort(resultList, new Comparator<HashMap.Entry<Integer, Double>>() {
			public int compare(HashMap.Entry<Integer, Double> o1, HashMap.Entry<Integer, Double> o2) {
				if (o1.getValue() == o2.getValue()) {
					return o2.getKey().compareTo(o1.getKey());
				}
				return o2.getValue().compareTo(o1.getValue());
			}
		});
	}
	//================================================================================================================
	// END RESULTMAP SORT FUNCTION

	
	
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis()/1000;
		String myString = "";
		restore();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("./report.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// test1
		myString = "//test  1: machine learning.";
		System.out.println("\n" + myString);
		writer.println(myString);
		ArrayList<String> test = new ArrayList<String>();
		ArrayList<String> result = new ArrayList<String>();
		test.add("machine");
		test.add("learning");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}

		// test2
		myString = "\n//test  2: mondego.";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("mondego");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}

		// test3
		myString = "\n//test  3: software engineering.";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("software");
		test.add("engineering");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}
		
		// test4
		myString = "\n//test  4: security.";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("security");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}
		
		// test5
		myString = "\n//test  5: student affairs";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("student");
		test.add("affairs");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}
		
		// test6
		myString = "\n//test  6: graduate courses";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("graduate");
		test.add("courses");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}
		
		// test7
		myString = "\n//test  7: informatics";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("informatics");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}
		
		// test8
		myString = "\n//test   8: REST";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("rest");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}
		
		// test9
		myString = "\n//test  9: computer games";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("computer");
		test.add("games");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}
		
		// test10
		myString = "\n//test 10: information retrieval";
		System.out.println(myString);
		writer.println(myString);
		test.clear();
		test.add("information");
		test.add("retrieval");
		result = getFinalResult(test);
		for (String s : result) {
			System.out.println(s);
			writer.println(s);
		}

		long end = System.currentTimeMillis()/1000;
		long totalTime = end - start;
		myString = "Finish time: " + Long.toString(totalTime) + "s.";
		writer.println("\n\n" + myString);
		writer.close();
		System.out.println(myString);
	}
}
