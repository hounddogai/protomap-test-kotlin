package com.acme.transport.lastmile.locker.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict locker target calculation. */
interface StrictLockerTargetStep {
    fun describe(): String
    fun adjust(item: StrictLockerTarget, value: Double): Double
}

data class CapStrictLockerTargetAdjuster(var cap: Double) : StrictLockerTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictLockerTarget, value: Double): Double = minOf(value, cap)
}

data class StrictLockerTargetCapacityOffsetAdjuster(var weight: Double) : StrictLockerTargetStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: StrictLockerTarget, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class PercentStrictLockerTargetAdjuster(var percent: Double) : StrictLockerTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictLockerTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitStrictLockerTargetAdjuster(var factors: MutableMap<String, Double>) : StrictLockerTargetStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: StrictLockerTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedStrictLockerTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictLockerTargetStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictLockerTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictLockerTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictLockerTargetStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictLockerTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict locker target steps in order and keeps a trace of each. */
data class StrictLockerTargetEngine(
    var steps: MutableList<StrictLockerTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictLockerTarget): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictLockerTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictLockerTargetEngine(flags: Registry): StrictLockerTargetEngine {
    return StrictLockerTargetEngine(
        steps = mutableListOf<StrictLockerTargetStep>(
            CapStrictLockerTargetAdjuster(37827.0),
            StrictLockerTargetCapacityOffsetAdjuster(1.14),
            PercentStrictLockerTargetAdjuster(18.4),
            UnitStrictLockerTargetAdjuster(mutableMapOf<String, Double>("east" to 1.33)),
            FlaggedStrictLockerTargetAdjuster(flags, "strict-locker-target-primary", -9.6),
            TieredStrictLockerTargetAdjuster(
                mutableListOf<Double>(4210.0, 4334.0, 6095.0),
                mutableListOf<Double>(0.91, 1.02, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 8107.6,
    )
}
