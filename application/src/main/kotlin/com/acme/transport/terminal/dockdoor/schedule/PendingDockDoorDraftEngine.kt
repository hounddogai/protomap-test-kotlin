package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.flags.Registry

/** One step of the pending dock door draft calculation. */
interface PendingDockDoorDraftStep {
    fun describe(): String
    fun adjust(item: PendingDockDoorDraft, value: Double): Double
}

data class TieredPendingDockDoorDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDockDoorDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDockDoorDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PendingDockDoorDraftMarginOffsetAdjuster(var weight: Double) : PendingDockDoorDraftStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: PendingDockDoorDraft, value: Double): Double = value + item.margin * weight
}

data class PercentPendingDockDoorDraftAdjuster(var percent: Double) : PendingDockDoorDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDockDoorDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingDockDoorDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingDockDoorDraftStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingDockDoorDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LanePendingDockDoorDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDockDoorDraftStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: PendingDockDoorDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending dock door draft steps in order and keeps a trace of each. */
data class PendingDockDoorDraftEngine(
    var steps: MutableList<PendingDockDoorDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingDockDoorDraft): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingDockDoorDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingDockDoorDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingDockDoorDraftEngine(flags: Registry): PendingDockDoorDraftEngine {
    return PendingDockDoorDraftEngine(
        steps = mutableListOf<PendingDockDoorDraftStep>(
            TieredPendingDockDoorDraftAdjuster(
                mutableListOf<Double>(2890.0, 3061.0),
                mutableListOf<Double>(0.99, 1.01),
            ),
            PendingDockDoorDraftMarginOffsetAdjuster(0.19),
            PercentPendingDockDoorDraftAdjuster(-3.6),
            FlaggedPendingDockDoorDraftAdjuster(flags, "pending-dock-door-draft-batched", 9.6),
            LanePendingDockDoorDraftAdjuster(mutableMapOf<String, Double>("east" to 0.73)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 35056.0,
    )
}
