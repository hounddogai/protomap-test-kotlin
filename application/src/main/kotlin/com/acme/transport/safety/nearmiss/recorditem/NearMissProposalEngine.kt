package com.acme.transport.safety.nearmiss.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss proposal calculation. */
interface NearMissProposalFactor {
    fun describe(): String
    fun adjust(item: NearMissProposal, value: Double): Double
}

data class TieredNearMissProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NearMissProposalFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NearMissProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedNearMissProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissProposalFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NearMissProposalDurationMinutesOffsetAdjuster(var weight: Double) : NearMissProposalFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: NearMissProposal, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapNearMissProposalAdjuster(var cap: Double) : NearMissProposalFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissProposal, value: Double): Double = minOf(value, cap)
}

/** Runs the near miss proposal steps in order and keeps a trace of each. */
data class NearMissProposalEngine(
    var steps: MutableList<NearMissProposalFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: NearMissProposal): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultNearMissProposalEngine(flags: Registry): NearMissProposalEngine {
    return NearMissProposalEngine(
        steps = mutableListOf<NearMissProposalFactor>(
            TieredNearMissProposalAdjuster(
                mutableListOf<Double>(4041.0, 9053.0),
                mutableListOf<Double>(1.11, 1.09),
            ),
            FlaggedNearMissProposalAdjuster(flags, "near-miss-proposal-capped", -3.2),
            NearMissProposalDurationMinutesOffsetAdjuster(1.33),
            CapNearMissProposalAdjuster(21202.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 26647.9,
    )
}
