package com.acme.transport.lastmile.locker.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the locker target calculation. */
interface LockerTargetAdjuster {
    fun describe(): String
    fun adjust(item: LockerTarget, value: Double): Double
}

data class MinimumLockerTargetAdjuster(var minimum: Double) : LockerTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedLockerTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryLockerTargetAdjuster(var factors: MutableMap<String, Double>) : LockerTargetAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: LockerTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the locker target steps in order and keeps a trace of each. */
data class LockerTargetEngine(
    var steps: MutableList<LockerTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LockerTarget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLockerTargetEngine(flags: Registry): LockerTargetEngine {
    return LockerTargetEngine(
        steps = mutableListOf<LockerTargetAdjuster>(
            MinimumLockerTargetAdjuster(31.0),
            FlaggedLockerTargetAdjuster(flags, "locker-target-pending", -4.4),
            CategoryLockerTargetAdjuster(
                mutableMapOf<String, Double>("east" to 0.79, "retail" to 0.92, "south" to 1.35),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 80008.2,
    )
}
