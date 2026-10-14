package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.flags.Registry

/** One step of the global zone tier calculation. */
interface GlobalZoneTierModifier {
    fun describe(): String
    fun adjust(item: GlobalZoneTier, value: Double): Double
}

data class PercentGlobalZoneTierAdjuster(var percent: Double) : GlobalZoneTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalZoneTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameGlobalZoneTierAdjuster(var factors: MutableMap<String, Double>) : GlobalZoneTierModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalZoneTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class GlobalZoneTierEtaMinutesOffsetAdjuster(var weight: Double) : GlobalZoneTierModifier {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: GlobalZoneTier, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class TieredGlobalZoneTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalZoneTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalZoneTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalZoneTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalZoneTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalZoneTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global zone tier steps in order and keeps a trace of each. */
data class GlobalZoneTierEngine(
    var steps: MutableList<GlobalZoneTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalZoneTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalZoneTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalZoneTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalZoneTierEngine(flags: Registry): GlobalZoneTierEngine {
    return GlobalZoneTierEngine(
        steps = mutableListOf<GlobalZoneTierModifier>(
            PercentGlobalZoneTierAdjuster(11.2),
            NameGlobalZoneTierAdjuster(
                mutableMapOf<String, Double>("north" to 0.84, "south" to 0.9, "retail" to 1.21),
            ),
            GlobalZoneTierEtaMinutesOffsetAdjuster(1.23),
            TieredGlobalZoneTierAdjuster(
                mutableListOf<Double>(4333.0, 6176.0, 8941.0, 8984.0),
                mutableListOf<Double>(0.91, 1.05, 1.16, 1.1),
            ),
            FlaggedGlobalZoneTierAdjuster(flags, "global-zone-tier-batched", -3.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 6113.8,
    )
}
