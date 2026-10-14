package com.acme.transport.terminal.dockdoor.ledger

fun globalDockDoorGroupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a global dock door group code in its canonical GDDG- form. */
fun normalizeGlobalDockDoorGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDDG-")) {
        return clean
    }
    return "GDDG-${clean.replace(" ", "-")}"
}
