package hcyxy.tech.core.service

private fun throwException(msg: String): Nothing {
    throw Exception(msg)
}

private fun <E : Exception> throwException(exception: E): Nothing {
    throw exception
}


fun ensure(predicate: Boolean, msg: String) {
    if (!predicate) throwException(msg)
}

fun ensure(predicate: Boolean, fn: () -> String) {
    if (!predicate) throwException(fn())
}

fun <E : Exception> ensure(predicate: Boolean, exception: E) {
    if (!predicate) throw exception
}

fun <T> notnull(obj: T?, msg: String): T = obj ?: throwException(msg)

fun <T> notnull(obj: T?, fn: () -> String): T = obj ?: throwException(fn())

fun <T, E : Exception> notnull(obj: T?, exception: E) = obj ?: throwException(exception)
