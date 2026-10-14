package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff claim calculation. */
interface CutoffClaimFactor {
    fun describe(): String
    fun adjust(item: CutoffClaim, value: Double): Double
}

data class FlaggedCutoffClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryCutoffClaimAdjuster(var factors: MutableMap<String, Double>) : CutoffClaimFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: CutoffClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCutoffClaimAdjuster(var step: Double) : CutoffClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffClaim, value: Double): Double = roundTo(value, step)
}

data class PercentCutoffClaimAdjuster(var percent: Double) : CutoffClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredCutoffClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the cutoff claim steps in order and keeps a trace of each. */
data class CutoffClaimEngine(
    var steps: MutableList<CutoffClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffClaim): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffClaimEngine(flags: Registry): CutoffClaimEngine {
    return CutoffClaimEngine(
        steps = mutableListOf<CutoffClaimFactor>(
            FlaggedCutoffClaimAdjuster(flags, "cutoff-claim-weighted", 6.6),
            CategoryCutoffClaimAdjuster(
                mutableMapOf<String, Double>("east" to 0.92, "retail" to 0.74, "west" to 0.76),
            ),
            RoundingCutoffClaimAdjuster(0.05),
            PercentCutoffClaimAdjuster(-24.4),
            TieredCutoffClaimAdjuster(mutableListOf<Double>(3556.0, 9123.0), mutableListOf<Double>(1.09, 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 60542.3,
    )
}
