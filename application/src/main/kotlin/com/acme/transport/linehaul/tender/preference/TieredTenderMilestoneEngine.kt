package com.acme.transport.linehaul.tender.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the tiered tender milestone calculation. */
interface TieredTenderMilestoneModifier {
    fun describe(): String
    fun adjust(item: TieredTenderMilestone, value: Double): Double
}

data class LabelTieredTenderMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredTenderMilestoneModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: TieredTenderMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTieredTenderMilestoneAdjuster(var cap: Double) : TieredTenderMilestoneModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTenderMilestone, value: Double): Double = minOf(value, cap)
}

data class TieredTenderMilestoneUpdatedAtOffsetAdjuster(var weight: Double) : TieredTenderMilestoneModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: TieredTenderMilestone, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class PercentTieredTenderMilestoneAdjuster(var percent: Double) : TieredTenderMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredTenderMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTieredTenderMilestoneAdjuster(var step: Double) : TieredTenderMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredTenderMilestone, value: Double): Double = roundTo(value, step)
}

data class FlaggedTieredTenderMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredTenderMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredTenderMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered tender milestone steps in order and keeps a trace of each. */
data class TieredTenderMilestoneEngine(
    var steps: MutableList<TieredTenderMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredTenderMilestone): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTenderMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTenderMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredTenderMilestoneEngine(flags: Registry): TieredTenderMilestoneEngine {
    return TieredTenderMilestoneEngine(
        steps = mutableListOf<TieredTenderMilestoneModifier>(
            LabelTieredTenderMilestoneAdjuster(mutableMapOf<String, Double>("north" to 1.07)),
            CapTieredTenderMilestoneAdjuster(9308.0),
            TieredTenderMilestoneUpdatedAtOffsetAdjuster(1.2),
            PercentTieredTenderMilestoneAdjuster(-19.1),
            RoundingTieredTenderMilestoneAdjuster(0.25),
            FlaggedTieredTenderMilestoneAdjuster(flags, "tiered-tender-milestone-seasonal", -7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 93691.0,
    )
}
