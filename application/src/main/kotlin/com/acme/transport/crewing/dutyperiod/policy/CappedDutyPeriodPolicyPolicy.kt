package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCappedDutyPeriodPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a capped duty period policy code in its canonical CDPP- form. */
fun normalizeCappedDutyPeriodPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDPP-")) {
        return clean
    }
    return "CDPP-${clean.replace(" ", "-")}"
}
