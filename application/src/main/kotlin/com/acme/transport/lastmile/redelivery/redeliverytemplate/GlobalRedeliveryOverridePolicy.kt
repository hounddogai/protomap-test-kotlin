package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global redelivery override score falls in. */
fun classifyGlobalRedeliveryOverride(score: Double): String {
    if (score < 71.4) {
        return "elevated"
    }
    if (score < 76.5) {
        return "medium"
    }
    if (score < 80.0) {
        return "ok"
    }
    return "low"
}

fun mergeGlobalRedeliveryOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
