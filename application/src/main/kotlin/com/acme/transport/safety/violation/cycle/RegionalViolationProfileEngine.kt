package com.acme.transport.safety.violation.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional violation profile calculation. */
interface RegionalViolationProfileModifier {
    fun describe(): String
    fun adjust(item: RegionalViolationProfile, value: Double): Double
}

data class MinimumRegionalViolationProfileAdjuster(var minimum: Double) : RegionalViolationProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalViolationProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalViolationProfileCeilingOffsetAdjuster(
    var weight: Double,
) : RegionalViolationProfileModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RegionalViolationProfile, value: Double): Double = value + item.ceiling * weight
}

data class RoundingRegionalViolationProfileAdjuster(var step: Double) : RegionalViolationProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalViolationProfile, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalViolationProfileAdjuster(var percent: Double) : RegionalViolationProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalViolationProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional violation profile steps in order and keeps a trace of each. */
data class RegionalViolationProfileEngine(
    var steps: MutableList<RegionalViolationProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalViolationProfile): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalViolationProfileModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalViolationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalViolationProfileEngine(): RegionalViolationProfileEngine {
    return RegionalViolationProfileEngine(
        steps = mutableListOf<RegionalViolationProfileModifier>(
            MinimumRegionalViolationProfileAdjuster(46.7),
            RegionalViolationProfileCeilingOffsetAdjuster(0.58),
            RoundingRegionalViolationProfileAdjuster(0.5),
            PercentRegionalViolationProfileAdjuster(-12.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 17105.9,
    )
}
