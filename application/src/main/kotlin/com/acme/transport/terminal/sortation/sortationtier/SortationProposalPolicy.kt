package com.acme.transport.terminal.sortation.sortationtier

import com.acme.transport.kit.numbers.roundTo

fun sortationProposalBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun sortationProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Puts a sortation proposal code in its canonical SP- form. */
fun normalizeSortationProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SP-")) {
        return clean
    }
    return "SP-${clean.replace(" ", "-")}"
}
