package com.acme.transport.emissions.fuelburn.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient fuel burn profile calculation. */
interface LenientFuelBurnProfileFactor {
    fun describe(): String
    fun adjust(item: LenientFuelBurnProfile, value: Double): Double
}

data class CountryLenientFuelBurnProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientFuelBurnProfileFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientFuelBurnProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLenientFuelBurnProfileAdjuster(var step: Double) : LenientFuelBurnProfileFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientFuelBurnProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientFuelBurnProfileAdjuster(var minimum: Double) : LenientFuelBurnProfileFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientFuelBurnProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientFuelBurnProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientFuelBurnProfileFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientFuelBurnProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientFuelBurnProfileAdjuster(var cap: Double) : LenientFuelBurnProfileFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientFuelBurnProfile, value: Double): Double = minOf(value, cap)
}

data class TieredLenientFuelBurnProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientFuelBurnProfileFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientFuelBurnProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient fuel burn profile steps in order and keeps a trace of each. */
data class LenientFuelBurnProfileEngine(
    var steps: MutableList<LenientFuelBurnProfileFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientFuelBurnProfile): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientFuelBurnProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientFuelBurnProfileEngine(flags: Registry): LenientFuelBurnProfileEngine {
    return LenientFuelBurnProfileEngine(
        steps = mutableListOf<LenientFuelBurnProfileFactor>(
            CountryLenientFuelBurnProfileAdjuster(
                mutableMapOf<String, Double>("online" to 1.04, "retail" to 0.97),
            ),
            RoundingLenientFuelBurnProfileAdjuster(5.0),
            MinimumLenientFuelBurnProfileAdjuster(42.6),
            FlaggedLenientFuelBurnProfileAdjuster(flags, "lenient-fuel-burn-profile-rolling", 0.9),
            CapLenientFuelBurnProfileAdjuster(37947.0),
            TieredLenientFuelBurnProfileAdjuster(
                mutableListOf<Double>(3297.0, 8776.0, 9563.0),
                mutableListOf<Double>(0.8, 0.96, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 21170.2,
    )
}
