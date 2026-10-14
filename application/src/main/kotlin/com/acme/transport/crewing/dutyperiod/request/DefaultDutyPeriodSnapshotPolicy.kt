package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a default duty period snapshot code in its canonical DDPS- form. */
fun normalizeDefaultDutyPeriodSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDPS-")) {
        return clean
    }
    return "DDPS-${clean.replace(" ", "-")}"
}

fun mergeDefaultDutyPeriodSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
