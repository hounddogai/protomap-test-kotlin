package com.acme.transport.crewing.restbreak.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break claim calculation. */
interface RestBreakClaimAdjuster {
    fun describe(): String
    fun adjust(item: RestBreakClaim, value: Double): Double
}

data class CapRestBreakClaimAdjuster(var cap: Double) : RestBreakClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakClaim, value: Double): Double = minOf(value, cap)
}

data class RestBreakClaimRateOffsetAdjuster(var weight: Double) : RestBreakClaimAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: RestBreakClaim, value: Double): Double = value + item.rate * weight
}

data class FlaggedRestBreakClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRestBreakClaimAdjuster(var step: Double) : RestBreakClaimAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the rest break claim steps in order and keeps a trace of each. */
data class RestBreakClaimEngine(
    var steps: MutableList<RestBreakClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakClaim): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakClaimAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakClaimEngine(flags: Registry): RestBreakClaimEngine {
    return RestBreakClaimEngine(
        steps = mutableListOf<RestBreakClaimAdjuster>(
            CapRestBreakClaimAdjuster(16500.0),
            RestBreakClaimRateOffsetAdjuster(0.95),
            FlaggedRestBreakClaimAdjuster(flags, "rest-break-claim-scheduled", -8.0),
            RoundingRestBreakClaimAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 3795.2,
    )
}
