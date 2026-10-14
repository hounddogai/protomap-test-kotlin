package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched declaration manifest calculation. */
interface BatchedDeclarationManifestModifier {
    fun describe(): String
    fun adjust(item: BatchedDeclarationManifest, value: Double): Double
}

data class RoundingBatchedDeclarationManifestAdjuster(var step: Double) : BatchedDeclarationManifestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedDeclarationManifest, value: Double): Double = roundTo(value, step)
}

data class ChannelBatchedDeclarationManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedDeclarationManifestModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: BatchedDeclarationManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBatchedDeclarationManifestAdjuster(var cap: Double) : BatchedDeclarationManifestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedDeclarationManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the batched declaration manifest steps in order and keeps a trace of each. */
data class BatchedDeclarationManifestEngine(
    var steps: MutableList<BatchedDeclarationManifestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BatchedDeclarationManifest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedDeclarationManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBatchedDeclarationManifestEngine(): BatchedDeclarationManifestEngine {
    return BatchedDeclarationManifestEngine(
        steps = mutableListOf<BatchedDeclarationManifestModifier>(
            RoundingBatchedDeclarationManifestAdjuster(5.0),
            ChannelBatchedDeclarationManifestAdjuster(
                mutableMapOf<String, Double>("online" to 1.28, "retail" to 1.38, "east" to 0.76),
            ),
            CapBatchedDeclarationManifestAdjuster(31875.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 59323.1,
    )
}
