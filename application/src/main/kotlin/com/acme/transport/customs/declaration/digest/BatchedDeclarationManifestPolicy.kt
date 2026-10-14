package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun batchedDeclarationManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a batched declaration manifest score falls in. */
fun classifyBatchedDeclarationManifest(score: Double): String {
    if (score < 4.4) {
        return "elevated"
    }
    if (score < 26.9) {
        return "watch"
    }
    if (score < 81.7) {
        return "high"
    }
    return "medium"
}

fun mergeBatchedDeclarationManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
