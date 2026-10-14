package com.acme.transport.network.node.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive node allocation calculation. */
interface AdaptiveNodeAllocationFactor {
    fun describe(): String
    fun adjust(item: AdaptiveNodeAllocation, value: Double): Double
}

data class CapAdaptiveNodeAllocationAdjuster(var cap: Double) : AdaptiveNodeAllocationFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveNodeAllocation, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveNodeAllocationAdjuster(var percent: Double) : AdaptiveNodeAllocationFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveNodeAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveNodeAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveNodeAllocationFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveNodeAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveNodeAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveNodeAllocationFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveNodeAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAdaptiveNodeAllocationAdjuster(var step: Double) : AdaptiveNodeAllocationFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveNodeAllocation, value: Double): Double = roundTo(value, step)
}

data class CategoryAdaptiveNodeAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveNodeAllocationFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: AdaptiveNodeAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive node allocation steps in order and keeps a trace of each. */
data class AdaptiveNodeAllocationEngine(
    var steps: MutableList<AdaptiveNodeAllocationFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveNodeAllocation): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveNodeAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveNodeAllocationEngine(flags: Registry): AdaptiveNodeAllocationEngine {
    return AdaptiveNodeAllocationEngine(
        steps = mutableListOf<AdaptiveNodeAllocationFactor>(
            CapAdaptiveNodeAllocationAdjuster(34341.0),
            PercentAdaptiveNodeAllocationAdjuster(-3.2),
            TieredAdaptiveNodeAllocationAdjuster(
                mutableListOf<Double>(4135.0, 6872.0),
                mutableListOf<Double>(0.91, 0.88),
            ),
            FlaggedAdaptiveNodeAllocationAdjuster(flags, "adaptive-node-allocation-seasonal", 0.5),
            RoundingAdaptiveNodeAllocationAdjuster(0.05),
            CategoryAdaptiveNodeAllocationAdjuster(mutableMapOf<String, Double>("north" to 1.1, "east" to 0.89)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 90437.8,
    )
}
