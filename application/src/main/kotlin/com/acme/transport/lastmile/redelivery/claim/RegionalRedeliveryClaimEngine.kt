package com.acme.transport.lastmile.redelivery.claim

/** One step of the regional redelivery claim calculation. */
interface RegionalRedeliveryClaimAdjuster {
    fun describe(): String
    fun adjust(item: RegionalRedeliveryClaim, value: Double): Double
}

data class TieredRegionalRedeliveryClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRedeliveryClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRedeliveryClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalRedeliveryClaimAdjuster(var cap: Double) : RegionalRedeliveryClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRedeliveryClaim, value: Double): Double = minOf(value, cap)
}

data class RegionalRedeliveryClaimGraceDaysOffsetAdjuster(
    var weight: Double,
) : RegionalRedeliveryClaimAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: RegionalRedeliveryClaim, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

/** Runs the regional redelivery claim steps in order and keeps a trace of each. */
data class RegionalRedeliveryClaimEngine(
    var steps: MutableList<RegionalRedeliveryClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalRedeliveryClaim): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRedeliveryClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRedeliveryClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalRedeliveryClaimEngine(): RegionalRedeliveryClaimEngine {
    return RegionalRedeliveryClaimEngine(
        steps = mutableListOf<RegionalRedeliveryClaimAdjuster>(
            TieredRegionalRedeliveryClaimAdjuster(
                mutableListOf<Double>(1092.0, 9823.0),
                mutableListOf<Double>(0.84, 1.02),
            ),
            CapRegionalRedeliveryClaimAdjuster(20911.0),
            RegionalRedeliveryClaimGraceDaysOffsetAdjuster(0.69),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 25377.6,
    )
}
