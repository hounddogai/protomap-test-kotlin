package com.acme.transport.safety.nearmiss.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the batched near miss manifest calculation. */
interface BatchedNearMissManifestAdjuster {
    fun describe(): String
    fun adjust(item: BatchedNearMissManifest, value: Double): Double
}

data class RoundingBatchedNearMissManifestAdjuster(var step: Double) : BatchedNearMissManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BatchedNearMissManifest, value: Double): Double = roundTo(value, step)
}

data class PercentBatchedNearMissManifestAdjuster(var percent: Double) : BatchedNearMissManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedNearMissManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedBatchedNearMissManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BatchedNearMissManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BatchedNearMissManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredBatchedNearMissManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedNearMissManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedNearMissManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedNearMissManifestAdjuster(var cap: Double) : BatchedNearMissManifestAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedNearMissManifest, value: Double): Double = minOf(value, cap)
}

data class TitleBatchedNearMissManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedNearMissManifestAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedNearMissManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched near miss manifest steps in order and keeps a trace of each. */
data class BatchedNearMissManifestEngine(
    var steps: MutableList<BatchedNearMissManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedNearMissManifest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BatchedNearMissManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedNearMissManifestEngine(flags: Registry): BatchedNearMissManifestEngine {
    return BatchedNearMissManifestEngine(
        steps = mutableListOf<BatchedNearMissManifestAdjuster>(
            RoundingBatchedNearMissManifestAdjuster(0.05),
            PercentBatchedNearMissManifestAdjuster(-1.5),
            FlaggedBatchedNearMissManifestAdjuster(flags, "batched-near-miss-manifest-tiered", -6.8),
            TieredBatchedNearMissManifestAdjuster(
                mutableListOf<Double>(1455.0, 8247.0, 9085.0),
                mutableListOf<Double>(1.09, 1.07, 0.91),
            ),
            CapBatchedNearMissManifestAdjuster(9218.0),
            TitleBatchedNearMissManifestAdjuster(mutableMapOf<String, Double>("west" to 1.34, "retail" to 1.21)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 57485.5,
    )
}
