package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a node notice code in its canonical NN- form. */
fun normalizeNodeNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NN-")) {
        return clean
    }
    return "NN-${clean.replace(" ", "-")}"
}

fun nodeNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeNodeNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a node notice score falls in. */
fun classifyNodeNotice(score: Double): String {
    if (score < 28.1) {
        return "critical"
    }
    if (score < 90.8) {
        return "ok"
    }
    return "medium"
}
