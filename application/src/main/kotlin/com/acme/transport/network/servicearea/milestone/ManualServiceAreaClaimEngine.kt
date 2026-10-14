package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual service area claim calculation. */
interface ManualServiceAreaClaimAdjuster {
    fun describe(): String
    fun adjust(item: ManualServiceAreaClaim, value: Double): Double
}

data class TieredManualServiceAreaClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualServiceAreaClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualServiceAreaClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualServiceAreaClaimAdjuster(var percent: Double) : ManualServiceAreaClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualServiceAreaClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualServiceAreaClaimAdjuster(var cap: Double) : ManualServiceAreaClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualServiceAreaClaim, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualServiceAreaClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualServiceAreaClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualServiceAreaClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualServiceAreaClaimAdjuster(var minimum: Double) : ManualServiceAreaClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualServiceAreaClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual service area claim steps in order and keeps a trace of each. */
data class ManualServiceAreaClaimEngine(
    var steps: MutableList<ManualServiceAreaClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualServiceAreaClaim): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualServiceAreaClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualServiceAreaClaimEngine(flags: Registry): ManualServiceAreaClaimEngine {
    return ManualServiceAreaClaimEngine(
        steps = mutableListOf<ManualServiceAreaClaimAdjuster>(
            TieredManualServiceAreaClaimAdjuster(
                mutableListOf<Double>(2687.0, 6291.0, 6791.0, 8863.0),
                mutableListOf<Double>(0.92, 0.84, 0.9, 1.04),
            ),
            PercentManualServiceAreaClaimAdjuster(-20.5),
            CapManualServiceAreaClaimAdjuster(38679.0),
            FlaggedManualServiceAreaClaimAdjuster(flags, "manual-service-area-claim-rolling", 9.9),
            MinimumManualServiceAreaClaimAdjuster(49.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 30122.8,
    )
}
