package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling incident score calculation. */
interface RollingIncidentScoreModifier {
    fun describe(): String
    fun adjust(item: RollingIncidentScore, value: Double): Double
}

data class PercentRollingIncidentScoreAdjuster(var percent: Double) : RollingIncidentScoreModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingIncidentScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingIncidentScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingIncidentScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingIncidentScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RollingIncidentScoreStopCountOffsetAdjuster(var weight: Double) : RollingIncidentScoreModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RollingIncidentScore, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the rolling incident score steps in order and keeps a trace of each. */
data class RollingIncidentScoreEngine(
    var steps: MutableList<RollingIncidentScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingIncidentScore): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingIncidentScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingIncidentScoreEngine(): RollingIncidentScoreEngine {
    return RollingIncidentScoreEngine(
        steps = mutableListOf<RollingIncidentScoreModifier>(
            PercentRollingIncidentScoreAdjuster(4.8),
            TieredRollingIncidentScoreAdjuster(
                mutableListOf<Double>(717.0, 4146.0, 6983.0),
                mutableListOf<Double>(0.96, 1.08, 0.98),
            ),
            RollingIncidentScoreStopCountOffsetAdjuster(1.64),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 73528.3,
    )
}
