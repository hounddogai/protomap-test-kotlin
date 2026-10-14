package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual cutoff claim calculation. */
interface ManualCutoffClaimModifier {
    fun describe(): String
    fun adjust(item: ManualCutoffClaim, value: Double): Double
}

data class PercentManualCutoffClaimAdjuster(var percent: Double) : ManualCutoffClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCutoffClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualCutoffClaimAdjuster(var minimum: Double) : ManualCutoffClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCutoffClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualCutoffClaimAdjuster(var cap: Double) : ManualCutoffClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCutoffClaim, value: Double): Double = minOf(value, cap)
}

data class ManualCutoffClaimAmountOffsetAdjuster(var weight: Double) : ManualCutoffClaimModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: ManualCutoffClaim, value: Double): Double = value + item.amount * weight
}

data class TieredManualCutoffClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualCutoffClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualCutoffClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual cutoff claim steps in order and keeps a trace of each. */
data class ManualCutoffClaimEngine(
    var steps: MutableList<ManualCutoffClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualCutoffClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualCutoffClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualCutoffClaimEngine(): ManualCutoffClaimEngine {
    return ManualCutoffClaimEngine(
        steps = mutableListOf<ManualCutoffClaimModifier>(
            PercentManualCutoffClaimAdjuster(13.0),
            MinimumManualCutoffClaimAdjuster(31.9),
            CapManualCutoffClaimAdjuster(38736.0),
            ManualCutoffClaimAmountOffsetAdjuster(0.18),
            TieredManualCutoffClaimAdjuster(
                mutableListOf<Double>(1784.0, 3609.0, 6279.0, 9069.0),
                mutableListOf<Double>(1.15, 1.2, 1.19, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 86752.4,
    )
}
