package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.flags.Registry

/** One step of the tender rollup calculation. */
interface TenderRollupModifier {
    fun describe(): String
    fun adjust(item: TenderRollup, value: Double): Double
}

data class LocaleTenderRollupAdjuster(var factors: MutableMap<String, Double>) : TenderRollupModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: TenderRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTenderRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TenderRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TenderRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTenderRollupAdjuster(var minimum: Double) : TenderRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentTenderRollupAdjuster(var percent: Double) : TenderRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTenderRollupAdjuster(var cap: Double) : TenderRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TenderRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the tender rollup steps in order and keeps a trace of each. */
data class TenderRollupEngine(
    var steps: MutableList<TenderRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TenderRollup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TenderRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTenderRollupEngine(flags: Registry): TenderRollupEngine {
    return TenderRollupEngine(
        steps = mutableListOf<TenderRollupModifier>(
            LocaleTenderRollupAdjuster(mutableMapOf<String, Double>("west" to 0.7)),
            FlaggedTenderRollupAdjuster(flags, "tender-rollup-fallback", -9.7),
            MinimumTenderRollupAdjuster(23.3),
            PercentTenderRollupAdjuster(9.7),
            CapTenderRollupAdjuster(25743.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 60795.0,
    )
}
