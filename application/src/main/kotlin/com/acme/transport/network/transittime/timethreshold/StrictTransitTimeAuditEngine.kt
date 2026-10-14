package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict transit time audit calculation. */
interface StrictTransitTimeAuditModifier {
    fun describe(): String
    fun adjust(item: StrictTransitTimeAudit, value: Double): Double
}

data class StrictTransitTimeAuditRetriesOffsetAdjuster(var weight: Double) : StrictTransitTimeAuditModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: StrictTransitTimeAudit, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class FlaggedStrictTransitTimeAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTransitTimeAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTransitTimeAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapStrictTransitTimeAuditAdjuster(var cap: Double) : StrictTransitTimeAuditModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTransitTimeAudit, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictTransitTimeAuditAdjuster(var step: Double) : StrictTransitTimeAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTransitTimeAudit, value: Double): Double = roundTo(value, step)
}

data class TieredStrictTransitTimeAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTransitTimeAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTransitTimeAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictTransitTimeAuditAdjuster(var minimum: Double) : StrictTransitTimeAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTransitTimeAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict transit time audit steps in order and keeps a trace of each. */
data class StrictTransitTimeAuditEngine(
    var steps: MutableList<StrictTransitTimeAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictTransitTimeAudit): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTransitTimeAuditModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTransitTimeAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictTransitTimeAuditEngine(flags: Registry): StrictTransitTimeAuditEngine {
    return StrictTransitTimeAuditEngine(
        steps = mutableListOf<StrictTransitTimeAuditModifier>(
            StrictTransitTimeAuditRetriesOffsetAdjuster(0.7),
            FlaggedStrictTransitTimeAuditAdjuster(flags, "strict-transit-time-audit-fallback", 2.7),
            CapStrictTransitTimeAuditAdjuster(19869.0),
            RoundingStrictTransitTimeAuditAdjuster(0.25),
            TieredStrictTransitTimeAuditAdjuster(
                mutableListOf<Double>(2008.0, 5826.0, 8231.0),
                mutableListOf<Double>(1.01, 1.1, 0.94),
            ),
            MinimumStrictTransitTimeAuditAdjuster(39.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 77114.6,
    )
}
