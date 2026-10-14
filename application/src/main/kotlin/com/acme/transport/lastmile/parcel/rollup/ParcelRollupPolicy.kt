package com.acme.transport.lastmile.parcel.rollup

/** Puts a parcel rollup code in its canonical PR- form. */
fun normalizeParcelRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PR-")) {
        return clean
    }
    return "PR-${clean.replace(" ", "-")}"
}

fun parcelRollupSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
