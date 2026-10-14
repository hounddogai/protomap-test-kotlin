package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient tariff claim calculation. */
interface LenientTariffClaimStep {
    fun describe(): String
    fun adjust(item: LenientTariffClaim, value: Double): Double
}

data class CapLenientTariffClaimAdjuster(var cap: Double) : LenientTariffClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientTariffClaim, value: Double): Double = minOf(value, cap)
}

data class LenientTariffClaimQuantityOffsetAdjuster(var weight: Double) : LenientTariffClaimStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LenientTariffClaim, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingLenientTariffClaimAdjuster(var step: Double) : LenientTariffClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientTariffClaim, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientTariffClaimAdjuster(var minimum: Double) : LenientTariffClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientTariffClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLenientTariffClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientTariffClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientTariffClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientTariffClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientTariffClaimStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientTariffClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient tariff claim steps in order and keeps a trace of each. */
data class LenientTariffClaimEngine(
    var steps: MutableList<LenientTariffClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientTariffClaim): Double {
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

    fun evaluateAll(items: MutableList<LenientTariffClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientTariffClaimEngine(flags: Registry): LenientTariffClaimEngine {
    return LenientTariffClaimEngine(
        steps = mutableListOf<LenientTariffClaimStep>(
            CapLenientTariffClaimAdjuster(1109.0),
            LenientTariffClaimQuantityOffsetAdjuster(0.18),
            RoundingLenientTariffClaimAdjuster(0.25),
            MinimumLenientTariffClaimAdjuster(32.8),
            FlaggedLenientTariffClaimAdjuster(flags, "lenient-tariff-claim-legacy", 7.0),
            TieredLenientTariffClaimAdjuster(
                mutableListOf<Double>(2900.0, 3834.0),
                mutableListOf<Double>(0.84, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 81791.2,
    )
}
