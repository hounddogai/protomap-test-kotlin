package com.acme.transport.network.node.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal node agreement calculation. */
interface SeasonalNodeAgreementStep {
    fun describe(): String
    fun adjust(item: SeasonalNodeAgreement, value: Double): Double
}

data class MinimumSeasonalNodeAgreementAdjuster(var minimum: Double) : SeasonalNodeAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalNodeAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalNodeAgreementAdjuster(var step: Double) : SeasonalNodeAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalNodeAgreement, value: Double): Double = roundTo(value, step)
}

data class CapSeasonalNodeAgreementAdjuster(var cap: Double) : SeasonalNodeAgreementStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalNodeAgreement, value: Double): Double = minOf(value, cap)
}

data class LabelSeasonalNodeAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalNodeAgreementStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalNodeAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalNodeAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalNodeAgreementStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalNodeAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalNodeAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalNodeAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalNodeAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal node agreement steps in order and keeps a trace of each. */
data class SeasonalNodeAgreementEngine(
    var steps: MutableList<SeasonalNodeAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalNodeAgreement): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalNodeAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalNodeAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalNodeAgreementEngine(flags: Registry): SeasonalNodeAgreementEngine {
    return SeasonalNodeAgreementEngine(
        steps = mutableListOf<SeasonalNodeAgreementStep>(
            MinimumSeasonalNodeAgreementAdjuster(12.2),
            RoundingSeasonalNodeAgreementAdjuster(1.0),
            CapSeasonalNodeAgreementAdjuster(24955.0),
            LabelSeasonalNodeAgreementAdjuster(mutableMapOf<String, Double>("online" to 0.7, "north" to 0.95)),
            TieredSeasonalNodeAgreementAdjuster(
                mutableListOf<Double>(3376.0, 4636.0, 6025.0, 9563.0),
                mutableListOf<Double>(0.86, 0.97, 1.01, 0.94),
            ),
            FlaggedSeasonalNodeAgreementAdjuster(flags, "seasonal-node-agreement-scheduled", -5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 66875.5,
    )
}
