package com.acme.transport.network.node.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped node variance calculation. */
interface CappedNodeVarianceModifier {
    fun describe(): String
    fun adjust(item: CappedNodeVariance, value: Double): Double
}

data class NameCappedNodeVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedNodeVarianceModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: CappedNodeVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedNodeVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedNodeVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedNodeVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedNodeVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedNodeVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedNodeVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedNodeVarianceAdjuster(var minimum: Double) : CappedNodeVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedNodeVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedNodeVarianceAdjuster(var step: Double) : CappedNodeVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedNodeVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the capped node variance steps in order and keeps a trace of each. */
data class CappedNodeVarianceEngine(
    var steps: MutableList<CappedNodeVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedNodeVariance): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedNodeVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedNodeVarianceEngine(flags: Registry): CappedNodeVarianceEngine {
    return CappedNodeVarianceEngine(
        steps = mutableListOf<CappedNodeVarianceModifier>(
            NameCappedNodeVarianceAdjuster(mutableMapOf<String, Double>("west" to 0.83, "retail" to 1.11)),
            FlaggedCappedNodeVarianceAdjuster(flags, "capped-node-variance-strict", -6.5),
            TieredCappedNodeVarianceAdjuster(
                mutableListOf<Double>(2278.0, 7110.0, 8479.0),
                mutableListOf<Double>(0.88, 0.86, 0.8),
            ),
            MinimumCappedNodeVarianceAdjuster(42.2),
            RoundingCappedNodeVarianceAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 36439.5,
    )
}
