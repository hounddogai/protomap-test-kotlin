package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a global coaching schedule code in its canonical GCS- form. */
fun normalizeGlobalCoachingScheduleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GCS-")) {
        return clean
    }
    return "GCS-${clean.replace(" ", "-")}"
}

fun mergeGlobalCoachingScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
