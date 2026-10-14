package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict backhaul audit calculation. */
interface StrictBackhaulAuditModifier {
    fun describe(): String
    fun adjust(item: StrictBackhaulAudit, value: Double): Double
}

data class TieredStrictBackhaulAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictBackhaulAuditModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictBackhaulAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictBackhaulAuditAdjuster(var minimum: Double) : StrictBackhaulAuditModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictBackhaulAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictBackhaulAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictBackhaulAuditModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictBackhaulAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictBackhaulAuditAdjuster(var step: Double) : StrictBackhaulAuditModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictBackhaulAudit, value: Double): Double = roundTo(value, step)
}

data class TitleStrictBackhaulAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictBackhaulAuditModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictBackhaulAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict backhaul audit steps in order and keeps a trace of each. */
data class StrictBackhaulAuditEngine(
    var steps: MutableList<StrictBackhaulAuditModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictBackhaulAudit): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictBackhaulAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictBackhaulAuditEngine(flags: Registry): StrictBackhaulAuditEngine {
    return StrictBackhaulAuditEngine(
        steps = mutableListOf<StrictBackhaulAuditModifier>(
            TieredStrictBackhaulAuditAdjuster(
                mutableListOf<Double>(68.0, 1092.0, 7185.0),
                mutableListOf<Double>(0.89, 0.82, 0.88),
            ),
            MinimumStrictBackhaulAuditAdjuster(43.7),
            FlaggedStrictBackhaulAuditAdjuster(flags, "strict-backhaul-audit-rolling", 8.2),
            RoundingStrictBackhaulAuditAdjuster(0.25),
            TitleStrictBackhaulAuditAdjuster(
                mutableMapOf<String, Double>("south" to 1.36, "west" to 0.76, "online" to 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 47887.6,
    )
}
