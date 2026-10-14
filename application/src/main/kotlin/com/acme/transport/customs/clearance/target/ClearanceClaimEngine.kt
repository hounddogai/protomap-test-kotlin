package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance claim calculation. */
interface ClearanceClaimFactor {
    fun describe(): String
    fun adjust(item: ClearanceClaim, value: Double): Double
}

data class ClearanceClaimCapacityOffsetAdjuster(var weight: Double) : ClearanceClaimFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ClearanceClaim, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class TieredClearanceClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentClearanceClaimAdjuster(var percent: Double) : ClearanceClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelClearanceClaimAdjuster(var factors: MutableMap<String, Double>) : ClearanceClaimFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: ClearanceClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingClearanceClaimAdjuster(var step: Double) : ClearanceClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceClaim, value: Double): Double = roundTo(value, step)
}

data class FlaggedClearanceClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the clearance claim steps in order and keeps a trace of each. */
data class ClearanceClaimEngine(
    var steps: MutableList<ClearanceClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceClaim): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceClaimEngine(flags: Registry): ClearanceClaimEngine {
    return ClearanceClaimEngine(
        steps = mutableListOf<ClearanceClaimFactor>(
            ClearanceClaimCapacityOffsetAdjuster(0.17),
            TieredClearanceClaimAdjuster(
                mutableListOf<Double>(789.0, 2588.0, 9591.0),
                mutableListOf<Double>(1.06, 1.05, 0.95),
            ),
            PercentClearanceClaimAdjuster(11.9),
            LabelClearanceClaimAdjuster(
                mutableMapOf<String, Double>("north" to 0.85, "online" to 1.33, "west" to 1.34),
            ),
            RoundingClearanceClaimAdjuster(5.0),
            FlaggedClearanceClaimAdjuster(flags, "clearance-claim-seasonal", -9.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 33818.3,
    )
}
