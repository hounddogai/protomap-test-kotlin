package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun batchedNodeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 82435.0, in order. */
fun batchedNodeMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82435.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeBatchedNodeMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a batched node milestone code in its canonical BNM- form. */
fun normalizeBatchedNodeMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BNM-")) {
        return clean
    }
    return "BNM-${clean.replace(" ", "-")}"
}
