package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan claim calculation. */
interface LanePlanClaimFactor {
    fun describe(): String
    fun adjust(item: LanePlanClaim, value: Double): Double
}

data class TieredLanePlanClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LanePlanClaimDistanceKmOffsetAdjuster(var weight: Double) : LanePlanClaimFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: LanePlanClaim, value: Double): Double = value + item.distanceKm * weight
}

data class FlaggedLanePlanClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLanePlanClaimAdjuster(var percent: Double) : LanePlanClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLanePlanClaimAdjuster(var step: Double) : LanePlanClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanClaim, value: Double): Double = roundTo(value, step)
}

data class DepotLanePlanClaimAdjuster(var factors: MutableMap<String, Double>) : LanePlanClaimFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: LanePlanClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lane plan claim steps in order and keeps a trace of each. */
data class LanePlanClaimEngine(
    var steps: MutableList<LanePlanClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LanePlanClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanClaimFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLanePlanClaimEngine(flags: Registry): LanePlanClaimEngine {
    return LanePlanClaimEngine(
        steps = mutableListOf<LanePlanClaimFactor>(
            TieredLanePlanClaimAdjuster(
                mutableListOf<Double>(3524.0, 4104.0, 5090.0),
                mutableListOf<Double>(0.9, 1.08, 1.19),
            ),
            LanePlanClaimDistanceKmOffsetAdjuster(1.35),
            FlaggedLanePlanClaimAdjuster(flags, "lane-plan-claim-weighted", 4.1),
            PercentLanePlanClaimAdjuster(4.7),
            RoundingLanePlanClaimAdjuster(0.5),
            DepotLanePlanClaimAdjuster(
                mutableMapOf<String, Double>("retail" to 1.3, "north" to 1.18, "online" to 1.27),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 32574.2,
    )
}
