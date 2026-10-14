package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal hold audit calculation. */
interface SeasonalHoldAuditFactor {
    fun describe(): String
    fun adjust(item: SeasonalHoldAudit, value: Double): Double
}

data class FlaggedSeasonalHoldAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalHoldAuditFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalHoldAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalHoldAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalHoldAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalHoldAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleSeasonalHoldAuditAdjuster(var factors: MutableMap<String, Double>) : SeasonalHoldAuditFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalHoldAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal hold audit steps in order and keeps a trace of each. */
data class SeasonalHoldAuditEngine(
    var steps: MutableList<SeasonalHoldAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalHoldAudit): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalHoldAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalHoldAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalHoldAuditEngine(flags: Registry): SeasonalHoldAuditEngine {
    return SeasonalHoldAuditEngine(
        steps = mutableListOf<SeasonalHoldAuditFactor>(
            FlaggedSeasonalHoldAuditAdjuster(flags, "seasonal-hold-audit-default", 3.4),
            TieredSeasonalHoldAuditAdjuster(
                mutableListOf<Double>(8988.0, 9064.0),
                mutableListOf<Double>(1.07, 0.8),
            ),
            TitleSeasonalHoldAuditAdjuster(
                mutableMapOf<String, Double>("retail" to 1.05, "online" to 0.72, "west" to 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 67983.1,
    )
}
