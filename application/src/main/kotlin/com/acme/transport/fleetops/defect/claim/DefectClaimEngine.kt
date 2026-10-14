package com.acme.transport.fleetops.defect.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the defect claim calculation. */
interface DefectClaimFactor {
    fun describe(): String
    fun adjust(item: DefectClaim, value: Double): Double
}

data class FlaggedDefectClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefectClaimFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefectClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumDefectClaimAdjuster(var minimum: Double) : DefectClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefectClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefectClaimAdjuster(var cap: Double) : DefectClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectClaim, value: Double): Double = minOf(value, cap)
}

data class PercentDefectClaimAdjuster(var percent: Double) : DefectClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefectClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefectClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefectClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefectClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleDefectClaimAdjuster(var factors: MutableMap<String, Double>) : DefectClaimFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: DefectClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the defect claim steps in order and keeps a trace of each. */
data class DefectClaimEngine(
    var steps: MutableList<DefectClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefectClaim): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefectClaimEngine(flags: Registry): DefectClaimEngine {
    return DefectClaimEngine(
        steps = mutableListOf<DefectClaimFactor>(
            FlaggedDefectClaimAdjuster(flags, "defect-claim-primary", -6.9),
            MinimumDefectClaimAdjuster(17.6),
            CapDefectClaimAdjuster(20394.0),
            PercentDefectClaimAdjuster(-1.7),
            TieredDefectClaimAdjuster(mutableListOf<Double>(122.0, 8874.0), mutableListOf<Double>(1.0, 1.14)),
            LocaleDefectClaimAdjuster(mutableMapOf<String, Double>("south" to 0.81)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 8854.8,
    )
}
