package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dispute quota calculation. */
interface DisputeQuotaFactor {
    fun describe(): String
    fun adjust(item: DisputeQuota, value: Double): Double
}

data class MinimumDisputeQuotaAdjuster(var minimum: Double) : DisputeQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DisputeQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDisputeQuotaAdjuster(var cap: Double) : DisputeQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DisputeQuota, value: Double): Double = minOf(value, cap)
}

data class RoundingDisputeQuotaAdjuster(var step: Double) : DisputeQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DisputeQuota, value: Double): Double = roundTo(value, step)
}

data class DisputeQuotaRateOffsetAdjuster(var weight: Double) : DisputeQuotaFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: DisputeQuota, value: Double): Double = value + item.rate * weight
}

data class FlaggedDisputeQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DisputeQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DisputeQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dispute quota steps in order and keeps a trace of each. */
data class DisputeQuotaEngine(
    var steps: MutableList<DisputeQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DisputeQuota): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DisputeQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DisputeQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDisputeQuotaEngine(flags: Registry): DisputeQuotaEngine {
    return DisputeQuotaEngine(
        steps = mutableListOf<DisputeQuotaFactor>(
            MinimumDisputeQuotaAdjuster(37.4),
            CapDisputeQuotaAdjuster(39659.0),
            RoundingDisputeQuotaAdjuster(0.25),
            DisputeQuotaRateOffsetAdjuster(1.86),
            FlaggedDisputeQuotaAdjuster(flags, "dispute-quota-primary", 9.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 22584.1,
    )
}
