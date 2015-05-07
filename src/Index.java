import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;


public class Index {

	private static HashSet<String> stopWordsList = stopWords();
	static Integer numWords = 0;
	public static Map<String, Integer> wordList = new HashMap<String, Integer>();
	
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
	
	static void termToTermID() throws FileNotFoundException
	{
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


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
