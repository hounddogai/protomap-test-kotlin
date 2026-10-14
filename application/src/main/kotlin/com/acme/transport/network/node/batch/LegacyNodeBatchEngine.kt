package com.acme.transport.network.node.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy node batch calculation. */
interface LegacyNodeBatchStep {
    fun describe(): String
    fun adjust(item: LegacyNodeBatch, value: Double): Double
}

data class FlaggedLegacyNodeBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyNodeBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyNodeBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyNodeBatchAdjuster(var step: Double) : LegacyNodeBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyNodeBatch, value: Double): Double = roundTo(value, step)
}

data class CapLegacyNodeBatchAdjuster(var cap: Double) : LegacyNodeBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyNodeBatch, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyNodeBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyNodeBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyNodeBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy node batch steps in order and keeps a trace of each. */
data class LegacyNodeBatchEngine(
    var steps: MutableList<LegacyNodeBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyNodeBatch): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyNodeBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyNodeBatchEngine(flags: Registry): LegacyNodeBatchEngine {
    return LegacyNodeBatchEngine(
        steps = mutableListOf<LegacyNodeBatchStep>(
            FlaggedLegacyNodeBatchAdjuster(flags, "legacy-node-batch-primary", 3.0),
            RoundingLegacyNodeBatchAdjuster(0.1),
            CapLegacyNodeBatchAdjuster(16088.0),
            TieredLegacyNodeBatchAdjuster(
                mutableListOf<Double>(2060.0, 3801.0, 7013.0, 9626.0),
                mutableListOf<Double>(0.97, 0.83, 0.83, 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 69365.6,
    )
}
