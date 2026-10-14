package com.acme.transport.customs.declaration.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a declaration milestone score falls in. */
fun classifyDeclarationMilestone(score: Double): String {
    if (score < 2.1) {
        return "medium"
    }
    if (score < 26.7) {
        return "minimal"
    }
    if (score < 46.3) {
        return "watch"
    }
    return "elevated"
}

fun mergeDeclarationMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
