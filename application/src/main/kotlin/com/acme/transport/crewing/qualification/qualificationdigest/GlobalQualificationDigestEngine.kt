package com.acme.transport.crewing.qualification.qualificationdigest

import com.acme.transport.kit.numbers.roundTo

/** One step of the global qualification digest calculation. */
interface GlobalQualificationDigestStep {
    fun describe(): String
    fun adjust(item: GlobalQualificationDigest, value: Double): Double
}

data class MinimumGlobalQualificationDigestAdjuster(var minimum: Double) : GlobalQualificationDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalQualificationDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingGlobalQualificationDigestAdjuster(var step: Double) : GlobalQualificationDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalQualificationDigest, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalQualificationDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalQualificationDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalQualificationDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalQualificationDigestAdjuster(var cap: Double) : GlobalQualificationDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalQualificationDigest, value: Double): Double = minOf(value, cap)
}

data class CountryGlobalQualificationDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalQualificationDigestStep {
    override fun describe(): String = "by country"

    override fun adjust(item: GlobalQualificationDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global qualification digest steps in order and keeps a trace of each. */
data class GlobalQualificationDigestEngine(
    var steps: MutableList<GlobalQualificationDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalQualificationDigest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalQualificationDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalQualificationDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalQualificationDigestEngine(): GlobalQualificationDigestEngine {
    return GlobalQualificationDigestEngine(
        steps = mutableListOf<GlobalQualificationDigestStep>(
            MinimumGlobalQualificationDigestAdjuster(49.8),
            RoundingGlobalQualificationDigestAdjuster(0.05),
            TieredGlobalQualificationDigestAdjuster(
                mutableListOf<Double>(1531.0, 7179.0),
                mutableListOf<Double>(0.94, 1.0),
            ),
            CapGlobalQualificationDigestAdjuster(1348.0),
            CountryGlobalQualificationDigestAdjuster(
                mutableMapOf<String, Double>("central" to 1.38, "east" to 0.81, "south" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 29365.6,
    )
}
