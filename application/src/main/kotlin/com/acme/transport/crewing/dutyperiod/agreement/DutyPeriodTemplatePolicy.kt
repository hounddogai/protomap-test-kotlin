package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDutyPeriodTemplateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a duty period template code in its canonical DPT- form. */
fun normalizeDutyPeriodTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DPT-")) {
        return clean
    }
    return "DPT-${clean.replace(" ", "-")}"
}
