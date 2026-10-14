package com.acme.transport.linehaul.capacity.capacitymapping

fun regionalCapacityMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a regional capacity mapping code in its canonical RCM- form. */
fun normalizeRegionalCapacityMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCM-")) {
        return clean
    }
    return "RCM-${clean.replace(" ", "-")}"
}
