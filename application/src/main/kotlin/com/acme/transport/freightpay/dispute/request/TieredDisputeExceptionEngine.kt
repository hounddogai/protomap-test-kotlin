package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.flags.Registry

/** One step of the tiered dispute exception calculation. */
interface TieredDisputeExceptionModifier {
    fun describe(): String
    fun adjust(item: TieredDisputeException, value: Double): Double
}

data class MinimumTieredDisputeExceptionAdjuster(var minimum: Double) : TieredDisputeExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredDisputeException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredDisputeExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredDisputeExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredDisputeException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTieredDisputeExceptionAdjuster(var cap: Double) : TieredDisputeExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredDisputeException, value: Double): Double = minOf(value, cap)
}

data class FlaggedTieredDisputeExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredDisputeExceptionModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredDisputeException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered dispute exception steps in order and keeps a trace of each. */
data class TieredDisputeExceptionEngine(
    var steps: MutableList<TieredDisputeExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredDisputeException): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredDisputeExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredDisputeException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredDisputeExceptionEngine(flags: Registry): TieredDisputeExceptionEngine {
    return TieredDisputeExceptionEngine(
        steps = mutableListOf<TieredDisputeExceptionModifier>(
            MinimumTieredDisputeExceptionAdjuster(34.2),
            TieredTieredDisputeExceptionAdjuster(
                mutableListOf<Double>(666.0, 1222.0, 6260.0, 6645.0),
                mutableListOf<Double>(1.17, 1.08, 1.19, 1.01),
            ),
            CapTieredDisputeExceptionAdjuster(29250.0),
            FlaggedTieredDisputeExceptionAdjuster(flags, "tiered-dispute-exception-seasonal", 7.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 73468.7,
    )
}
