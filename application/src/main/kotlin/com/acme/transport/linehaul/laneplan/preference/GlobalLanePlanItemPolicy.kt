package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a global lane plan item code in its canonical GLPI- form. */
fun normalizeGlobalLanePlanItemCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GLPI-")) {
        return clean
    }
    return "GLPI-${clean.replace(" ", "-")}"
}

fun mergeGlobalLanePlanItemLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
