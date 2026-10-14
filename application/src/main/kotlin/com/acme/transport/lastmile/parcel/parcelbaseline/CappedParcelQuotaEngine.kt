package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped parcel quota calculation. */
interface CappedParcelQuotaModifier {
    fun describe(): String
    fun adjust(item: CappedParcelQuota, value: Double): Double
}

data class TieredCappedParcelQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedParcelQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedParcelQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedParcelQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedParcelQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedParcelQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCappedParcelQuotaAdjuster(var step: Double) : CappedParcelQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedParcelQuota, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedParcelQuotaAdjuster(var minimum: Double) : CappedParcelQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedParcelQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedParcelQuotaAdjuster(var percent: Double) : CappedParcelQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedParcelQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCappedParcelQuotaAdjuster(var cap: Double) : CappedParcelQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedParcelQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the capped parcel quota steps in order and keeps a trace of each. */
data class CappedParcelQuotaEngine(
    var steps: MutableList<CappedParcelQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedParcelQuota): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedParcelQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedParcelQuotaEngine(flags: Registry): CappedParcelQuotaEngine {
    return CappedParcelQuotaEngine(
        steps = mutableListOf<CappedParcelQuotaModifier>(
            TieredCappedParcelQuotaAdjuster(
                mutableListOf<Double>(3557.0, 7703.0, 7880.0),
                mutableListOf<Double>(0.84, 0.85, 0.95),
            ),
            FlaggedCappedParcelQuotaAdjuster(flags, "capped-parcel-quota-primary", 1.9),
            RoundingCappedParcelQuotaAdjuster(0.5),
            MinimumCappedParcelQuotaAdjuster(18.8),
            PercentCappedParcelQuotaAdjuster(-6.1),
            CapCappedParcelQuotaAdjuster(21877.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 58331.6,
    )
}
