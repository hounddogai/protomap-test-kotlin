package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePendingDutyPeriodExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending duty period exception code in its canonical PDPE- form. */
fun normalizePendingDutyPeriodExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDPE-")) {
        return clean
    }
    return "PDPE-${clean.replace(" ", "-")}"
}
