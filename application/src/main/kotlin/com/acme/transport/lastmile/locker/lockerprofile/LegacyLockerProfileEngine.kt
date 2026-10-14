package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy locker profile calculation. */
interface LegacyLockerProfileAdjuster {
    fun describe(): String
    fun adjust(item: LegacyLockerProfile, value: Double): Double
}

data class CapLegacyLockerProfileAdjuster(var cap: Double) : LegacyLockerProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLockerProfile, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyLockerProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLockerProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLockerProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyLockerProfileAdjuster(var step: Double) : LegacyLockerProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLockerProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy locker profile steps in order and keeps a trace of each. */
data class LegacyLockerProfileEngine(
    var steps: MutableList<LegacyLockerProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyLockerProfile): Double {
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

    fun evaluateAll(items: MutableList<LegacyLockerProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyLockerProfileEngine(): LegacyLockerProfileEngine {
    return LegacyLockerProfileEngine(
        steps = mutableListOf<LegacyLockerProfileAdjuster>(
            CapLegacyLockerProfileAdjuster(42035.0),
            TieredLegacyLockerProfileAdjuster(
                mutableListOf<Double>(1373.0, 9004.0),
                mutableListOf<Double>(1.01, 1.08),
            ),
            RoundingLegacyLockerProfileAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 10459.4,
    )
}
