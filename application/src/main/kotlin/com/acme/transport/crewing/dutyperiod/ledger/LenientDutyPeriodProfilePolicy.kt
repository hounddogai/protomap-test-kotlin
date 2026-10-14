package com.acme.transport.crewing.dutyperiod.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a lenient duty period profile code in its canonical LDPP- form. */
fun normalizeLenientDutyPeriodProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDPP-")) {
        return clean
    }
    return "LDPP-${clean.replace(" ", "-")}"
}

fun mergeLenientDutyPeriodProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
