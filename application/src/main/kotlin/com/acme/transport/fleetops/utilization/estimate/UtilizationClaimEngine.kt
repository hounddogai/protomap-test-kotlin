package com.acme.transport.fleetops.utilization.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the utilization claim calculation. */
interface UtilizationClaimStep {
    fun describe(): String
    fun adjust(item: UtilizationClaim, value: Double): Double
}

data class DepotUtilizationClaimAdjuster(var factors: MutableMap<String, Double>) : UtilizationClaimStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: UtilizationClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedUtilizationClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumUtilizationClaimAdjuster(var minimum: Double) : UtilizationClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredUtilizationClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingUtilizationClaimAdjuster(var step: Double) : UtilizationClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the utilization claim steps in order and keeps a trace of each. */
data class UtilizationClaimEngine(
    var steps: MutableList<UtilizationClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationClaim): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationClaimEngine(flags: Registry): UtilizationClaimEngine {
    return UtilizationClaimEngine(
        steps = mutableListOf<UtilizationClaimStep>(
            DepotUtilizationClaimAdjuster(mutableMapOf<String, Double>("west" to 0.88)),
            FlaggedUtilizationClaimAdjuster(flags, "utilization-claim-fallback", -5.5),
            MinimumUtilizationClaimAdjuster(0.3),
            TieredUtilizationClaimAdjuster(
                mutableListOf<Double>(828.0, 3438.0, 7663.0, 9601.0),
                mutableListOf<Double>(1.1, 0.89, 0.81, 1.1),
            ),
            RoundingUtilizationClaimAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 55868.7,
    )
}
