package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling defect proposal calculation. */
interface RollingDefectProposalModifier {
    fun describe(): String
    fun adjust(item: RollingDefectProposal, value: Double): Double
}

data class RollingDefectProposalDistanceKmOffsetAdjuster(var weight: Double) : RollingDefectProposalModifier {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: RollingDefectProposal, value: Double): Double = value + item.distanceKm * weight
}

data class CapRollingDefectProposalAdjuster(var cap: Double) : RollingDefectProposalModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingDefectProposal, value: Double): Double = minOf(value, cap)
}

data class PercentRollingDefectProposalAdjuster(var percent: Double) : RollingDefectProposalModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDefectProposal, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingDefectProposalAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDefectProposalModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDefectProposal, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingDefectProposalAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingDefectProposalModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingDefectProposal, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling defect proposal steps in order and keeps a trace of each. */
data class RollingDefectProposalEngine(
    var steps: MutableList<RollingDefectProposalModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingDefectProposal): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDefectProposal>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingDefectProposalEngine(flags: Registry): RollingDefectProposalEngine {
    return RollingDefectProposalEngine(
        steps = mutableListOf<RollingDefectProposalModifier>(
            RollingDefectProposalDistanceKmOffsetAdjuster(1.92),
            CapRollingDefectProposalAdjuster(29856.0),
            PercentRollingDefectProposalAdjuster(-15.0),
            TieredRollingDefectProposalAdjuster(
                mutableListOf<Double>(184.0, 1317.0, 3578.0),
                mutableListOf<Double>(0.97, 0.99, 1.04),
            ),
            FlaggedRollingDefectProposalAdjuster(flags, "rolling-defect-proposal-capped", -4.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 28411.0,
    )
}
