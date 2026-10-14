package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled locker claim calculation. */
interface ScheduledLockerClaimFactor {
    fun describe(): String
    fun adjust(item: ScheduledLockerClaim, value: Double): Double
}

data class TieredScheduledLockerClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledLockerClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledLockerClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledLockerClaimAdjuster(var cap: Double) : ScheduledLockerClaimFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledLockerClaim, value: Double): Double = minOf(value, cap)
}

data class CategoryScheduledLockerClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLockerClaimFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledLockerClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled locker claim steps in order and keeps a trace of each. */
data class ScheduledLockerClaimEngine(
    var steps: MutableList<ScheduledLockerClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLockerClaim): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledLockerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLockerClaimEngine(): ScheduledLockerClaimEngine {
    return ScheduledLockerClaimEngine(
        steps = mutableListOf<ScheduledLockerClaimFactor>(
            TieredScheduledLockerClaimAdjuster(
                mutableListOf<Double>(2410.0, 4630.0, 5424.0),
                mutableListOf<Double>(0.94, 1.08, 0.95),
            ),
            CapScheduledLockerClaimAdjuster(22281.0),
            CategoryScheduledLockerClaimAdjuster(
                mutableMapOf<String, Double>("west" to 1.21, "north" to 0.83, "retail" to 1.3),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 80272.8,
    )
}
