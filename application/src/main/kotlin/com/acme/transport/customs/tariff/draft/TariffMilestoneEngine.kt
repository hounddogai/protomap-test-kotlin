package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff milestone calculation. */
interface TariffMilestoneAdjuster {
    fun describe(): String
    fun adjust(item: TariffMilestone, value: Double): Double
}

data class RoundingTariffMilestoneAdjuster(var step: Double) : TariffMilestoneAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffMilestone, value: Double): Double = roundTo(value, step)
}

data class NameTariffMilestoneAdjuster(var factors: MutableMap<String, Double>) : TariffMilestoneAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TariffMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTariffMilestoneAdjuster(var cap: Double) : TariffMilestoneAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffMilestone, value: Double): Double = minOf(value, cap)
}

data class FlaggedTariffMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TariffMilestoneAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TariffMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tariff milestone steps in order and keeps a trace of each. */
data class TariffMilestoneEngine(
    var steps: MutableList<TariffMilestoneAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TariffMilestone): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffMilestoneAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TariffMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTariffMilestoneEngine(flags: Registry): TariffMilestoneEngine {
    return TariffMilestoneEngine(
        steps = mutableListOf<TariffMilestoneAdjuster>(
            RoundingTariffMilestoneAdjuster(0.5),
            NameTariffMilestoneAdjuster(mutableMapOf<String, Double>("east" to 1.26, "south" to 1.15)),
            CapTariffMilestoneAdjuster(25753.0),
            FlaggedTariffMilestoneAdjuster(flags, "tariff-milestone-pending", -0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 20895.8,
    )
}
