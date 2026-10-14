package com.acme.transport.lastmile.redelivery.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled redelivery digest calculation. */
interface ScheduledRedeliveryDigestStep {
    fun describe(): String
    fun adjust(item: ScheduledRedeliveryDigest, value: Double): Double
}

data class ScheduledRedeliveryDigestLimitOffsetAdjuster(var weight: Double) : ScheduledRedeliveryDigestStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: ScheduledRedeliveryDigest, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class TieredScheduledRedeliveryDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRedeliveryDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRedeliveryDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledRedeliveryDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRedeliveryDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRedeliveryDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled redelivery digest steps in order and keeps a trace of each. */
data class ScheduledRedeliveryDigestEngine(
    var steps: MutableList<ScheduledRedeliveryDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRedeliveryDigest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledRedeliveryDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRedeliveryDigestEngine(flags: Registry): ScheduledRedeliveryDigestEngine {
    return ScheduledRedeliveryDigestEngine(
        steps = mutableListOf<ScheduledRedeliveryDigestStep>(
            ScheduledRedeliveryDigestLimitOffsetAdjuster(1.92),
            TieredScheduledRedeliveryDigestAdjuster(
                mutableListOf<Double>(375.0, 2151.0),
                mutableListOf<Double>(1.17, 0.83),
            ),
            FlaggedScheduledRedeliveryDigestAdjuster(flags, "scheduled-redelivery-digest-strict", -2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 43584.2,
    )
}
