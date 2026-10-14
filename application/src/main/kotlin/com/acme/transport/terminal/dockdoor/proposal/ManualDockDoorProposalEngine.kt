package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual dock door proposal calculation. */
interface ManualDockDoorProposalAdjuster {
    fun describe(): String
    fun adjust(item: ManualDockDoorProposal, value: Double): Double
}

data class FlaggedManualDockDoorProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDockDoorProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDockDoorProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleManualDockDoorProposalAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDockDoorProposalAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualDockDoorProposal, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualDockDoorProposalAdjuster(var minimum: Double) : ManualDockDoorProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDockDoorProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual dock door proposal steps in order and keeps a trace of each. */
data class ManualDockDoorProposalEngine(
    var steps: MutableList<ManualDockDoorProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualDockDoorProposal): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDockDoorProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualDockDoorProposalEngine(flags: Registry): ManualDockDoorProposalEngine {
    return ManualDockDoorProposalEngine(
        steps = mutableListOf<ManualDockDoorProposalAdjuster>(
            FlaggedManualDockDoorProposalAdjuster(flags, "manual-dock-door-proposal-pending", -3.1),
            TitleManualDockDoorProposalAdjuster(mutableMapOf<String, Double>("online" to 1.36, "south" to 1.35)),
            MinimumManualDockDoorProposalAdjuster(19.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 92580.2,
    )
}
