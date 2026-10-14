package com.acme.transport.fleetops.tire.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tire profile calculation. */
interface TireProfileAdjuster {
    fun describe(): String
    fun adjust(item: TireProfile, value: Double): Double
}

data class RoundingTireProfileAdjuster(var step: Double) : TireProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireProfile, value: Double): Double = roundTo(value, step)
}

data class TireProfileRevisionOffsetAdjuster(var weight: Double) : TireProfileAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: TireProfile, value: Double): Double = value + item.revision.toDouble() * weight
}

data class NameTireProfileAdjuster(var factors: MutableMap<String, Double>) : TireProfileAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TireProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTireProfileAdjuster(var percent: Double) : TireProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TireProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tire profile steps in order and keeps a trace of each. */
data class TireProfileEngine(
    var steps: MutableList<TireProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TireProfile): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TireProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTireProfileEngine(): TireProfileEngine {
    return TireProfileEngine(
        steps = mutableListOf<TireProfileAdjuster>(
            RoundingTireProfileAdjuster(5.0),
            TireProfileRevisionOffsetAdjuster(0.71),
            NameTireProfileAdjuster(
                mutableMapOf<String, Double>("north" to 1.04, "east" to 1.01, "west" to 1.12),
            ),
            PercentTireProfileAdjuster(-7.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 11968.7,
    )
}
