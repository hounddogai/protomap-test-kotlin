package com.acme.transport.terminal.sortation.manifest

fun legacySortationManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a legacy sortation manifest code in its canonical LSM- form. */
fun normalizeLegacySortationManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSM-")) {
        return clean
    }
    return "LSM-${clean.replace(" ", "-")}"
}
