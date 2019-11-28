import java.util.stream.Stream;
import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.*;

// The main plagiarism detection program.
// You only need to change buildIndex() and findSimilarity().
public class Lab3 {
    public static void main(String[] args) {
        try {
            String directory;
            if (args.length == 0) {
                System.out.print("Name of directory to scan: ");
                System.out.flush();
                directory = new Scanner(System.in).nextLine();
            } else directory = args[0];
            Path[] paths = Files.list(Paths.get(directory)).toArray(Path[]::new);
            Arrays.sort(paths);

            // Stopwatches time how long each phase of the program
            // takes to execute.
            Stopwatch stopwatch = new Stopwatch();
            Stopwatch stopwatch2 = new Stopwatch();

            // Read all input files
            MinimalistMap<Path, Ngram[]> files = readPaths(paths);
            stopwatch.finished("Reading all input files");

            // Build index of n-grams (not implemented yet)
            MinimalistMap<Ngram, ArrayList<Path>> index = buildIndex(files);
            stopwatch.finished("Building n-gram index");

            // Compute similarity of all file pairs
            MinimalistMap<PathPair, Integer> similarity = findSimilarity(files, index);
            stopwatch.finished("Computing similarity scores");

            // Find most similar file pairs, arranged in
            // decreasing order of similarity
            ArrayList<PathPair> mostSimilar = findMostSimilar(similarity);
            stopwatch.finished("Finding the most similar files");
            stopwatch2.finished("In total the program");

            // Print out some statistics
            System.out.println("\nBST balance statistics:");
            if (files instanceof MinimalistTreeMap)
                System.out.printf("  files: size %d, height %d\n",
                        ((MinimalistTreeMap)files).size(), ((MinimalistTreeMap)files).height());
            if (index instanceof MinimalistTreeMap)
                System.out.printf("  index: size %d, height %d\n",
                        ((MinimalistTreeMap)index).size(), ((MinimalistTreeMap)index).height());
            if (similarity instanceof MinimalistTreeMap)
                System.out.printf("  similarity: size %d, height %d\n",
                        ((MinimalistTreeMap)similarity).size(), ((MinimalistTreeMap)similarity).height());
            System.out.println("");

            // Print out the plagiarism report!
            System.out.println("Plagiarism report:");
            for (PathPair pair: mostSimilar)
                System.out.printf("%5d similarity: %s\n", similarity.get(pair), pair);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Phase 1: Read in each file and chop it into n-grams.
    static MinimalistMap<Path, Ngram[]> readPaths(Path[] paths) throws IOException {
        MinimalistMap<Path, Ngram[]> files = new WrappedHashMap<>();
        for (Path path: paths) {
            String contents = new String(Files.readAllBytes(path));
            Ngram[] ngrams = Ngram.ngrams(contents, 5);
            // Remove duplicates from the ngrams list
            // Uses the Java 8 streams API - very handy Java feature
            // which we don't cover in the course. If you want to
            // learn about it, see e.g.
            // https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#package.description
            // or https://stackify.com/streams-guide-java-8/
            ngrams = Arrays.stream(ngrams).distinct().toArray(Ngram[]::new);
            files.put(path, ngrams);
        }

        return files;
    }

    // Phase 2: build index of n-grams
    static MinimalistMap<Ngram, ArrayList<Path>> buildIndex(MinimalistMap<Path, Ngram[]> files) {
        MinimalistMap<Ngram, ArrayList<Path>> index = new WrappedHashMap<>();
        for (Path file:files.keys()) {
            // create an array of all n-grams in a given file, for each file
            Ngram[] containedNgrams=files.get(file);
            for (Ngram ngram:containedNgrams) {
                // add the file being checked to the index for the current n-gram
                ArrayList<Path> indexedPaths=index.get(ngram);
                if (indexedPaths==null) {
                    // if this is the first time the ngram is found, initialize its List
                    indexedPaths=new ArrayList<>();
                    index.put(ngram,indexedPaths);
                }
                indexedPaths.add(file);
            }
        }
        return index;
    }

    // Phase 3: Count how many n-grams each pair of files has in common.
    static MinimalistMap<PathPair, Integer> findSimilarity(MinimalistMap<Path, Ngram[]> files, MinimalistMap<Ngram, ArrayList<Path>> index) {
        // N.B. Path is Java's class for representing filenames
        // PathPair represents a pair of Paths (see PathPair.java)
        MinimalistMap<PathPair, Integer> similarity = new WrappedHashMap<>();
        for (Ngram indexedNgram:index.keys()) {
            // get a list of all files containing each n-gram
            List<Path> containingFiles=index.get(indexedNgram);
            for (int i=0;i<containingFiles.size();i++) {
                for (int j=i+1;j<containingFiles.size();j++) {
                    // create every possible combination and increment their occurrence
                    PathPair pair=new PathPair(containingFiles.get(i),containingFiles.get(j));
                    Integer occurrences=similarity.get(pair);
                    if (occurrences==null) {
                        occurrences=0;
                    }
                    similarity.put(pair,occurrences+1);
                }
            }
        }
        return similarity;
    }

    // Phase 4: find all pairs of files with more than 30 n-grams
    // in common, sorted in descending order of similarity.
    static ArrayList<PathPair> findMostSimilar(MinimalistMap<PathPair, Integer> similarity) {
        // Find all pairs of files with more than 100 n-grams in common.
        ArrayList<PathPair> mostSimilar = new ArrayList<>();
        for (PathPair pair: similarity.keys()) {
            if (similarity.get(pair) < 30) continue;
            // Only consider each pair of files once - (a, b) and not
            // (b,a) - and also skip pairs consisting of the same file twice
            //if (pair.path1.compareTo(pair.path2) >= 0) continue;

            mostSimilar.add(pair);
        }

        // Sort to have the most similar pairs first.
        Collections.sort(mostSimilar, Comparator.comparing(pair -> similarity.get(pair)));
        Collections.reverse(mostSimilar);
        return mostSimilar;
    }
}
