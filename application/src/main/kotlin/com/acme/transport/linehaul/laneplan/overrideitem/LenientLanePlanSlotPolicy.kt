package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeLenientLanePlanSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a lenient lane plan slot code in its canonical LLPS- form. */
fun normalizeLenientLanePlanSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLPS-")) {
        return clean
    }
    return "LLPS-${clean.replace(" ", "-")}"
}
