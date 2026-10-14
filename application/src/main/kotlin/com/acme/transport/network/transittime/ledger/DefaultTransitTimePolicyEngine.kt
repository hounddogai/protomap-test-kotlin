package com.acme.transport.network.transittime.ledger

import com.acme.transport.kit.flags.Registry

/** One step of the default transit time policy calculation. */
interface DefaultTransitTimePolicyModifier {
    fun describe(): String
    fun adjust(item: DefaultTransitTimePolicy, value: Double): Double
}

data class TieredDefaultTransitTimePolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTransitTimePolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTransitTimePolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelDefaultTransitTimePolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultTransitTimePolicyModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultTransitTimePolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultTransitTimePolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTransitTimePolicyModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTransitTimePolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultTransitTimePolicyAdjuster(var percent: Double) : DefaultTransitTimePolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTransitTimePolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultTransitTimePolicyDistanceKmOffsetAdjuster(
    var weight: Double,
) : DefaultTransitTimePolicyModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: DefaultTransitTimePolicy, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the default transit time policy steps in order and keeps a trace of each. */
data class DefaultTransitTimePolicyEngine(
    var steps: MutableList<DefaultTransitTimePolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultTransitTimePolicy): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTransitTimePolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTransitTimePolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultTransitTimePolicyEngine(flags: Registry): DefaultTransitTimePolicyEngine {
    return DefaultTransitTimePolicyEngine(
        steps = mutableListOf<DefaultTransitTimePolicyModifier>(
            TieredDefaultTransitTimePolicyAdjuster(
                mutableListOf<Double>(5018.0, 8423.0),
                mutableListOf<Double>(1.06, 1.09),
            ),
            LabelDefaultTransitTimePolicyAdjuster(
                mutableMapOf<String, Double>("west" to 1.02, "north" to 1.34, "east" to 1.15),
            ),
            FlaggedDefaultTransitTimePolicyAdjuster(flags, "default-transit-time-policy-legacy", -9.2),
            PercentDefaultTransitTimePolicyAdjuster(22.3),
            DefaultTransitTimePolicyDistanceKmOffsetAdjuster(0.13),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 52597.6,
    )
}
