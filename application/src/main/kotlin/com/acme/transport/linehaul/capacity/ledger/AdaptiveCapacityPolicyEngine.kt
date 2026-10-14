package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive capacity policy calculation. */
interface AdaptiveCapacityPolicyModifier {
    fun describe(): String
    fun adjust(item: AdaptiveCapacityPolicy, value: Double): Double
}

data class TieredAdaptiveCapacityPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveCapacityPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveCapacityPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveCapacityPolicyAdjuster(var cap: Double) : AdaptiveCapacityPolicyModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCapacityPolicy, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveCapacityPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveCapacityPolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveCapacityPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveCapacityPolicyAdjuster(var minimum: Double) : AdaptiveCapacityPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCapacityPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAdaptiveCapacityPolicyAdjuster(var percent: Double) : AdaptiveCapacityPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveCapacityPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveCapacityPolicyFuelLitersOffsetAdjuster(var weight: Double) : AdaptiveCapacityPolicyModifier {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: AdaptiveCapacityPolicy, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the adaptive capacity policy steps in order and keeps a trace of each. */
data class AdaptiveCapacityPolicyEngine(
    var steps: MutableList<AdaptiveCapacityPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveCapacityPolicy): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCapacityPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCapacityPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveCapacityPolicyEngine(flags: Registry): AdaptiveCapacityPolicyEngine {
    return AdaptiveCapacityPolicyEngine(
        steps = mutableListOf<AdaptiveCapacityPolicyModifier>(
            TieredAdaptiveCapacityPolicyAdjuster(
                mutableListOf<Double>(6119.0, 8171.0, 8678.0),
                mutableListOf<Double>(0.81, 0.98, 0.82),
            ),
            CapAdaptiveCapacityPolicyAdjuster(9046.0),
            FlaggedAdaptiveCapacityPolicyAdjuster(flags, "adaptive-capacity-policy-lenient", 6.1),
            MinimumAdaptiveCapacityPolicyAdjuster(46.5),
            PercentAdaptiveCapacityPolicyAdjuster(-22.0),
            AdaptiveCapacityPolicyFuelLitersOffsetAdjuster(1.61),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 2174.1,
    )
}
