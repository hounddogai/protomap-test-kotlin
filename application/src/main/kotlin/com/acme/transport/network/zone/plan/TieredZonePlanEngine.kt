package com.acme.transport.network.zone.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered zone plan calculation. */
interface TieredZonePlanFactor {
    fun describe(): String
    fun adjust(item: TieredZonePlan, value: Double): Double
}

data class RoundingTieredZonePlanAdjuster(var step: Double) : TieredZonePlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredZonePlan, value: Double): Double = roundTo(value, step)
}

data class ChannelTieredZonePlanAdjuster(var factors: MutableMap<String, Double>) : TieredZonePlanFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: TieredZonePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTieredZonePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredZonePlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredZonePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTieredZonePlanAdjuster(var cap: Double) : TieredZonePlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredZonePlan, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered zone plan steps in order and keeps a trace of each. */
data class TieredZonePlanEngine(
    var steps: MutableList<TieredZonePlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredZonePlan): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredZonePlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredZonePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredZonePlanEngine(flags: Registry): TieredZonePlanEngine {
    return TieredZonePlanEngine(
        steps = mutableListOf<TieredZonePlanFactor>(
            RoundingTieredZonePlanAdjuster(1.0),
            ChannelTieredZonePlanAdjuster(mutableMapOf<String, Double>("east" to 1.17)),
            FlaggedTieredZonePlanAdjuster(flags, "tiered-zone-plan-pending", 2.8),
            CapTieredZonePlanAdjuster(20841.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 14429.9,
    )
}
