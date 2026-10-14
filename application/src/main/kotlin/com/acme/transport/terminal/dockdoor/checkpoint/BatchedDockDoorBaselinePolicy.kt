package com.acme.transport.terminal.dockdoor.checkpoint

fun batchedDockDoorBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Puts a batched dock door baseline code in its canonical BDDB- form. */
fun normalizeBatchedDockDoorBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BDDB-")) {
        return clean
    }
    return "BDDB-${clean.replace(" ", "-")}"
}
