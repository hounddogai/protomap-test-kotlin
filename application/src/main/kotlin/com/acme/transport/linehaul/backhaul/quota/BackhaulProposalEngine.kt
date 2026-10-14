package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul proposal calculation. */
interface BackhaulProposalAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulProposal, value: Double): Double
}

data class BackhaulProposalExpiresAtOffsetAdjuster(var weight: Double) : BackhaulProposalAdjuster {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: BackhaulProposal, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class MinimumBackhaulProposalAdjuster(var minimum: Double) : BackhaulProposalAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulProposal, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBackhaulProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BackhaulProposalAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BackhaulProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBackhaulProposalAdjuster(var cap: Double) : BackhaulProposalAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulProposal, value: Double): Double = minOf(value, cap)
}

data class RoundingBackhaulProposalAdjuster(var step: Double) : BackhaulProposalAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulProposal, value: Double): Double = roundTo(value, step)
}

/** Runs the backhaul proposal steps in order and keeps a trace of each. */
data class BackhaulProposalEngine(
    var steps: MutableList<BackhaulProposalAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BackhaulProposal): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBackhaulProposalEngine(flags: Registry): BackhaulProposalEngine {
    return BackhaulProposalEngine(
        steps = mutableListOf<BackhaulProposalAdjuster>(
            BackhaulProposalExpiresAtOffsetAdjuster(1.54),
            MinimumBackhaulProposalAdjuster(3.4),
            FlaggedBackhaulProposalAdjuster(flags, "backhaul-proposal-scheduled", -1.1),
            CapBackhaulProposalAdjuster(19822.0),
            RoundingBackhaulProposalAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 96487.7,
    )
}
