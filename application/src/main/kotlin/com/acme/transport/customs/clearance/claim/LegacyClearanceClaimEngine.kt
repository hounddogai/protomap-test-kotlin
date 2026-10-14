package com.acme.transport.customs.clearance.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy clearance claim calculation. */
interface LegacyClearanceClaimFactor {
    fun describe(): String
    fun adjust(item: LegacyClearanceClaim, value: Double): Double
}

data class FlaggedLegacyClearanceClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyClearanceClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyClearanceClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyClearanceClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyClearanceClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyClearanceClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameLegacyClearanceClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyClearanceClaimFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyClearanceClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyClearanceClaimUpdatedAtOffsetAdjuster(var weight: Double) : LegacyClearanceClaimFactor {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: LegacyClearanceClaim, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class RoundingLegacyClearanceClaimAdjuster(var step: Double) : LegacyClearanceClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyClearanceClaim, value: Double): Double = roundTo(value, step)
}

data class PercentLegacyClearanceClaimAdjuster(var percent: Double) : LegacyClearanceClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyClearanceClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy clearance claim steps in order and keeps a trace of each. */
data class LegacyClearanceClaimEngine(
    var steps: MutableList<LegacyClearanceClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyClearanceClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyClearanceClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyClearanceClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyClearanceClaimEngine(flags: Registry): LegacyClearanceClaimEngine {
    return LegacyClearanceClaimEngine(
        steps = mutableListOf<LegacyClearanceClaimFactor>(
            FlaggedLegacyClearanceClaimAdjuster(flags, "legacy-clearance-claim-primary", -5.3),
            TieredLegacyClearanceClaimAdjuster(
                mutableListOf<Double>(921.0, 2557.0, 9553.0),
                mutableListOf<Double>(1.14, 1.07, 1.04),
            ),
            NameLegacyClearanceClaimAdjuster(mutableMapOf<String, Double>("retail" to 1.0)),
            LegacyClearanceClaimUpdatedAtOffsetAdjuster(0.75),
            RoundingLegacyClearanceClaimAdjuster(0.05),
            PercentLegacyClearanceClaimAdjuster(-0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 39030.5,
    )
}
