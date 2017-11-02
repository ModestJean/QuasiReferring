import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

class Main {
    private final static Map<String,Integer> tokensFrequencies = new TreeMap<>();
    private final static int compressionPercent = 30;

    public static void main(String[] args) {
        Map<String,List<String>> sentenceToTokenList = MyStemRunner.runMyStem()
                .stream()
                .flatMap(Main::splitIntoSentences)

                .collect(Collectors.toMap(Main::removeTokens, Main::fetchTokenList));

        sentenceToTokenList.forEach((sentence, tokens) -> tokens.forEach(Main::countFrequency));

        // TODO: Remove stop-words

        Map<String,Integer> sentenceToWeight = sentenceToTokenList.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, map -> map.getValue()
                        .stream()
                        .reduce(0, (acc, token) -> acc + tokensFrequencies.get(token), (y,z)->y+z)));

        // TODO: Sorted output

        sentenceToWeight
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue((x,y)->y-x))
                .limit(sentenceToWeight.size() * compressionPercent / 100)
                .forEach(x->System.out.println(x.getKey() + " W=" + x.getValue()));
    }

    private static Stream<String> splitIntoSentences(String paragraph) {
        return Arrays.stream(paragraph.split(" \\{\\\\s}"));
    }

    private static List<String> fetchTokenList(String sentence) {
        // This matcher will capture first token from the token group surrounded by '{' and '}' and generated by MyStem
        // Example: {выходить|выхаживать} -> выходить
        Matcher tokenMatcher = Pattern.compile("(?<=\\{).*?(?=[}|])").matcher(sentence);
        if (tokenMatcher.find())
            return Stream
                    .generate(tokenMatcher::group)
                    .takeWhile(x -> tokenMatcher.find())
                    .collect(toCollection(ArrayList::new));
        return new ArrayList<>();
    }

    private static String removeTokens(String sentence) {
        return sentence.replaceAll("\\{.*?}","");
    }

    private static void countFrequency(String token) {
        tokensFrequencies.put(token, tokensFrequencies.containsKey(token) ? tokensFrequencies.get(token) + 1 : 1);
    }
}