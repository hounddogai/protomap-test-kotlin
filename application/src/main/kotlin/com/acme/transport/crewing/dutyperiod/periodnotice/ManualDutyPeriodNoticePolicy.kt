package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeManualDutyPeriodNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a manual duty period notice code in its canonical MDPN- form. */
fun normalizeManualDutyPeriodNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MDPN-")) {
        return clean
    }
    return "MDPN-${clean.replace(" ", "-")}"
}
