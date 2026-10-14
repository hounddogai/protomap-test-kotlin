package com.acme.transport.kit.ids

// Identifiers and references.

fun newId(prefix: String, seq: Int): String = "${prefix.uppercase()}-${seq.toString().padStart(6, '0')}"

fun shortHash(s: String): String = ((s.hashCode() and Int.MAX_VALUE) % 100000).toString().padStart(5, '0')

/** Reports whether s looks like an identifier NewID made with prefix. */
fun isReference(s: String, prefix: String): Boolean {
    if (!s.startsWith(prefix.uppercase() + "-")) {
        return false
    }
    return s.length == prefix.length + 7
}

fun withCheckDigit(s: String): String = "$s${(s.hashCode() and Int.MAX_VALUE) % 10}"
