package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict trailer yard audit calculation. */
interface StrictTrailerYardAuditAdjuster {
    fun describe(): String
    fun adjust(item: StrictTrailerYardAudit, value: Double): Double
}

data class PercentStrictTrailerYardAuditAdjuster(var percent: Double) : StrictTrailerYardAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTrailerYardAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictTrailerYardAuditAdjuster(var cap: Double) : StrictTrailerYardAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTrailerYardAudit, value: Double): Double = minOf(value, cap)
}

data class RoundingStrictTrailerYardAuditAdjuster(var step: Double) : StrictTrailerYardAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTrailerYardAudit, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictTrailerYardAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTrailerYardAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTrailerYardAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleStrictTrailerYardAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictTrailerYardAuditAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictTrailerYardAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictTrailerYardAuditAdjuster(var minimum: Double) : StrictTrailerYardAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTrailerYardAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict trailer yard audit steps in order and keeps a trace of each. */
data class StrictTrailerYardAuditEngine(
    var steps: MutableList<StrictTrailerYardAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictTrailerYardAudit): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTrailerYardAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTrailerYardAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictTrailerYardAuditEngine(flags: Registry): StrictTrailerYardAuditEngine {
    return StrictTrailerYardAuditEngine(
        steps = mutableListOf<StrictTrailerYardAuditAdjuster>(
            PercentStrictTrailerYardAuditAdjuster(20.5),
            CapStrictTrailerYardAuditAdjuster(11809.0),
            RoundingStrictTrailerYardAuditAdjuster(0.1),
            FlaggedStrictTrailerYardAuditAdjuster(flags, "strict-trailer-yard-audit-fallback", 0.2),
            TitleStrictTrailerYardAuditAdjuster(mutableMapOf<String, Double>("east" to 0.93, "central" to 1.35)),
            MinimumStrictTrailerYardAuditAdjuster(2.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 45314.7,
    )
}
