package ru.spbau.mit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SecondPartTasks {

    private SecondPartTasks() {
    }

    private static final int MAX_NUMBER = 1000000;
    private static final Random RANDOM = new Random();

    // Найти строки из переданных файлов, в которых встречается указанная подстрока.
    public static List<String> findQuotes(List<String> paths, CharSequence sequence) {
        try {
            return paths.stream().flatMap(path -> {
                try {
                    return Files.lines(Paths.get(path));
                } catch (IOException e) {
                    throw new UncheckedIOException("No file: ", e);
                }
            }).filter(s -> s.contains(sequence)).collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // В квадрат с длиной стороны 1 вписана мишень.
    // Стрелок атакует мишень и каждый раз попадает в произвольную точку квадрата.
    // Надо промоделировать этот процесс с помощью класса java.util.Random и посчитать, какова вероятность попасть в мишень.
    public static double piDividedBy4() {
        return Stream.generate(() -> Math.pow(RANDOM.nextDouble() - 0.5, 2) + Math.pow(RANDOM.nextDouble() - 0.5, 2))
                .limit(MAX_NUMBER)
                .filter(n -> n <= 0.25).count() * 1.0 / MAX_NUMBER;

    }

    // Дано отображение из имени автора в список с содержанием его произведений.
    // Надо вычислить, чья общая длина произведений наибольшая.
    public static String findPrinter(Map<String, List<String>> compositions) {
        return compositions.entrySet().stream()
                .collect(Collectors.maxBy(
                                Comparator.comparing(
                                        e -> e.getValue().stream().mapToInt(String::length).sum()
                                )
                        )
                ).map(Map.Entry::getKey).orElse(null);
    }

    // Вы крупный поставщик продуктов. Каждая торговая сеть делает вам заказ в виде Map<Товар, Количество>.
    // Необходимо вычислить, какой товар и в каком количестве надо поставить.
    public static Map<String, Integer> calculateGlobalOrder(List<Map<String, Integer>> orders) {
        return orders.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a1, a2) -> a1 + a2
                ));

    }
}
