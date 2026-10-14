package com.acme.transport.safety.violation.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the violation estimate calculation. */
interface ViolationEstimateFactor {
    fun describe(): String
    fun adjust(item: ViolationEstimate, value: Double): Double
}

data class MinimumViolationEstimateAdjuster(var minimum: Double) : ViolationEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ViolationEstimatePriorityOffsetAdjuster(var weight: Double) : ViolationEstimateFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: ViolationEstimate, value: Double): Double = value + item.priority.toDouble() * weight
}

data class RegionViolationEstimateAdjuster(var factors: MutableMap<String, Double>) : ViolationEstimateFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: ViolationEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedViolationEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredViolationEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the violation estimate steps in order and keeps a trace of each. */
data class ViolationEstimateEngine(
    var steps: MutableList<ViolationEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationEstimate): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationEstimateFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationEstimateEngine(flags: Registry): ViolationEstimateEngine {
    return ViolationEstimateEngine(
        steps = mutableListOf<ViolationEstimateFactor>(
            MinimumViolationEstimateAdjuster(31.6),
            ViolationEstimatePriorityOffsetAdjuster(0.98),
            RegionViolationEstimateAdjuster(
                mutableMapOf<String, Double>("west" to 1.3, "retail" to 0.82, "north" to 1.31),
            ),
            FlaggedViolationEstimateAdjuster(flags, "violation-estimate-lenient", 7.7),
            TieredViolationEstimateAdjuster(
                mutableListOf<Double>(475.0, 4432.0, 7936.0, 9730.0),
                mutableListOf<Double>(1.13, 0.97, 1.08, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 38322.2,
    )
}
