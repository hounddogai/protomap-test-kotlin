package com.acme.transport.network.node.ledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeTieredNodeLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun tieredNodeLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tiered node ledger code in its canonical TNL- form. */
fun normalizeTieredNodeLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TNL-")) {
        return clean
    }
    return "TNL-${clean.replace(" ", "-")}"
}

/** Names the band a tiered node ledger score falls in. */
fun classifyTieredNodeLedger(score: Double): String {
    if (score < 20.9) {
        return "medium"
    }
    if (score < 36.8) {
        return "low"
    }
    return "critical"
}
