package com.acme.transport.network.hub.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional hub agreement calculation. */
interface RegionalHubAgreementAdjuster {
    fun describe(): String
    fun adjust(item: RegionalHubAgreement, value: Double): Double
}

data class TieredRegionalHubAgreementAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalHubAgreementAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalHubAgreement, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalHubAgreementAdjuster(var step: Double) : RegionalHubAgreementAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalHubAgreement, value: Double): Double = roundTo(value, step)
}

data class ChannelRegionalHubAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalHubAgreementAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalHubAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalHubAgreementEtaMinutesOffsetAdjuster(var weight: Double) : RegionalHubAgreementAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: RegionalHubAgreement, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class CapRegionalHubAgreementAdjuster(var cap: Double) : RegionalHubAgreementAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalHubAgreement, value: Double): Double = minOf(value, cap)
}

/** Runs the regional hub agreement steps in order and keeps a trace of each. */
data class RegionalHubAgreementEngine(
    var steps: MutableList<RegionalHubAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalHubAgreement): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalHubAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalHubAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalHubAgreementEngine(): RegionalHubAgreementEngine {
    return RegionalHubAgreementEngine(
        steps = mutableListOf<RegionalHubAgreementAdjuster>(
            TieredRegionalHubAgreementAdjuster(
                mutableListOf<Double>(1177.0, 2749.0, 6154.0),
                mutableListOf<Double>(0.91, 0.96, 0.93),
            ),
            RoundingRegionalHubAgreementAdjuster(1.0),
            ChannelRegionalHubAgreementAdjuster(mutableMapOf<String, Double>("south" to 1.27)),
            RegionalHubAgreementEtaMinutesOffsetAdjuster(1.74),
            CapRegionalHubAgreementAdjuster(13337.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 50011.1,
    )
}
