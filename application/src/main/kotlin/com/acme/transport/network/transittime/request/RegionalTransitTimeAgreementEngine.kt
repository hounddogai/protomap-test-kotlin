package com.acme.transport.network.transittime.request

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional transit time agreement calculation. */
interface RegionalTransitTimeAgreementStep {
    fun describe(): String
    fun adjust(item: RegionalTransitTimeAgreement, value: Double): Double
}

data class NameRegionalTransitTimeAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalTransitTimeAgreementStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalTransitTimeAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalTransitTimeAgreementAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTransitTimeAgreementStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTransitTimeAgreement, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalTransitTimeAgreementAdjuster(var step: Double) : RegionalTransitTimeAgreementStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalTransitTimeAgreement, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalTransitTimeAgreementAdjuster(var minimum: Double) : RegionalTransitTimeAgreementStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTransitTimeAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalTransitTimeAgreementMarginOffsetAdjuster(
    var weight: Double,
) : RegionalTransitTimeAgreementStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: RegionalTransitTimeAgreement, value: Double): Double = value + item.margin * weight
}

data class PercentRegionalTransitTimeAgreementAdjuster(var percent: Double) : RegionalTransitTimeAgreementStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTransitTimeAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional transit time agreement steps in order and keeps a trace of each. */
data class RegionalTransitTimeAgreementEngine(
    var steps: MutableList<RegionalTransitTimeAgreementStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalTransitTimeAgreement): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTransitTimeAgreementStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTransitTimeAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalTransitTimeAgreementEngine(flags: Registry): RegionalTransitTimeAgreementEngine {
    return RegionalTransitTimeAgreementEngine(
        steps = mutableListOf<RegionalTransitTimeAgreementStep>(
            NameRegionalTransitTimeAgreementAdjuster(
                mutableMapOf<String, Double>("retail" to 0.92, "central" to 0.95),
            ),
            FlaggedRegionalTransitTimeAgreementAdjuster(flags, "regional-transit-time-agreement-tiered", -3.1),
            RoundingRegionalTransitTimeAgreementAdjuster(1.0),
            MinimumRegionalTransitTimeAgreementAdjuster(16.1),
            RegionalTransitTimeAgreementMarginOffsetAdjuster(0.98),
            PercentRegionalTransitTimeAgreementAdjuster(-6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 9453.5,
    )
}
