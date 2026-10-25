package lab2.types

/**
 * Компаратор — внешняя функция сравнения (требование lab2).
 * В отличие от lab1 (interface Comparator), в Scala используется type alias
 * на функцию: компаратор передаётся явно параметром в сортировку.
 */
type Comparator[A] = (A, A) => Int
