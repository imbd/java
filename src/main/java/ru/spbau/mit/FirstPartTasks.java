package ru.spbau.mit;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class FirstPartTasks {

    private FirstPartTasks() {
    }

    // Список названий альбомов
    public static List<String> allNames(Stream<Album> albums) {
        return albums.map(album -> album.getName())
                .collect(Collectors.toList());
    }

    // Список названий альбомов, отсортированный лексикографически по названию
    public static List<String> allNamesSorted(Stream<Album> albums) {
        return albums.sorted((a1, a2) -> a1.getName()
                .compareTo(a2.getName()))
                .map(album -> album.getName())
                .collect(Collectors.toList());
    }

    // Список треков, отсортированный лексикографически по названию, включающий все треки альбомов из 'albums'
    public static List<String> allTracksSorted(Stream<Album> albums) {
        return albums.flatMap(album -> album.getTracks().stream())
                .map(track -> track.getName())
                .sorted()
                .collect(Collectors.toList());

    }

    // Список альбомов, в которых есть хотя бы один трек с рейтингом более 95, отсортированный по названию
    public static List<Album> sortedFavorites(Stream<Album> s) {
        return s.filter(album -> album.getTracks().size() > 0).filter(album -> album.getTracks().stream().mapToInt(i -> i.getRating()).max().getAsInt() > 95)
                .sorted((a1, a2) -> a1.getName().compareTo(a2.getName()))
                .collect(Collectors.toList());
    }

    // Сгруппировать альбомы по артистам
    public static Map<Artist, List<Album>> groupByArtist(Stream<Album> albums) {
        return albums.collect(
                Collectors.groupingBy(Album::getArtist)
        );
    }

    // Сгруппировать альбомы по артистам (в качестве значения вместо объекта 'Artist' использовать его имя)
    public static Map<Artist, List<String>> groupByArtistMapName(Stream<Album> albums) {
        return albums.collect(
                Collectors.groupingBy(
                        Album::getArtist,
                        Collectors.mapping(Album::getName, Collectors.toList())
                )
        );
    }

    // Число повторяющихся альбомов в потоке
    public static long countAlbumDuplicates(Stream<Album> albums) {
        return albums.collect(
                Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                )
        ).entrySet().stream().filter(pair -> pair.getValue() > 1).count();

    }

    // Альбом, в котором максимум рейтинга минимален
    // (если в альбоме нет ни одного трека, считать, что максимум рейтинга в нем --- 0)
    public static Optional<Album> minMaxRating(Stream<Album> albums) {

        return albums.sorted(Comparator.comparing(album -> album.getTracks().stream().mapToInt(i -> i.getRating()).max().getAsInt()))
                .limit(1).collect(Collectors.minBy(Comparator.comparing(a -> 0)));
        //throw new UnsupportedOperationException();
    }

    // Список альбомов, отсортированный по убыванию среднего рейтинга его треков (0, если треков нет)
    public static List<Album> sortByAverageRating(Stream<Album> albums) {
        return albums.sorted(Comparator.comparing(album -> album.getTracks().stream().collect(Collectors.averagingInt(tr -> tr.getRating()))))
                .collect(
                        ArrayList::new,
                        (list, e) -> list.add(0, e),
                        (list1, list2) -> list1.addAll(0, list2)
                );
    }

    // Произведение всех чисел потока по модулю 'modulo'
    // (все числа от 0 до 10000)
    public static int moduloProduction(IntStream stream, int modulo) {
        return stream.reduce(1, (n1, n2) -> (n1 * n2) % modulo);
    }

    // Вернуть строку, состояющую из конкатенаций переданного массива, и окруженную строками "<", ">"
    // см. тесты
    public static String joinTo(String... strings) {
        return Arrays.asList(strings).stream().collect(
                Collectors.joining(", ", "<", ">"));
    }

    // Вернуть поток из объектов класса 'clazz'
    public static <R> Stream<R> filterIsInstance(Stream<?> s, Class<R> clazz) {
        return s.filter(el -> clazz.isAssignableFrom(el.getClass())).map(el -> (R) el);
        //throw new UnsupportedOperationException();
    }
}