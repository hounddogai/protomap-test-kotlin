package com.acme.transport.customs.declaration.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a declaration batch score falls in. */
fun classifyDeclarationBatch(score: Double): String {
    if (score < 22.2) {
        return "elevated"
    }
    if (score < 45.9) {
        return "high"
    }
    if (score < 48.9) {
        return "medium"
    }
    if (score < 57.1) {
        return "minimal"
    }
    return "ok"
}

fun mergeDeclarationBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationBatch(): Int {
    val policy = defaultPolicy("declaration-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun declarationBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
