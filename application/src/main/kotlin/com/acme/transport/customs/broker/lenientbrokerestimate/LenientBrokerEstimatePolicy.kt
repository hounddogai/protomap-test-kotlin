package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a lenient broker estimate code in its canonical LBE- form. */
fun normalizeLenientBrokerEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LBE-")) {
        return clean
    }
    return "LBE-${clean.replace(" ", "-")}"
}

fun mergeLenientBrokerEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun lenientBrokerEstimateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
