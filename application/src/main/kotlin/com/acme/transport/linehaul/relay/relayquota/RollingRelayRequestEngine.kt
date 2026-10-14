package com.acme.transport.linehaul.relay.relayquota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling relay request calculation. */
interface RollingRelayRequestAdjuster {
    fun describe(): String
    fun adjust(item: RollingRelayRequest, value: Double): Double
}

data class RollingRelayRequestFloorValueOffsetAdjuster(var weight: Double) : RollingRelayRequestAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: RollingRelayRequest, value: Double): Double = value + item.floorValue * weight
}

data class RoundingRollingRelayRequestAdjuster(var step: Double) : RollingRelayRequestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingRelayRequest, value: Double): Double = roundTo(value, step)
}

data class TieredRollingRelayRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingRelayRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingRelayRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelRollingRelayRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingRelayRequestAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingRelayRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRollingRelayRequestAdjuster(var percent: Double) : RollingRelayRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingRelayRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling relay request steps in order and keeps a trace of each. */
data class RollingRelayRequestEngine(
    var steps: MutableList<RollingRelayRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingRelayRequest): Double {
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

    fun evaluateAll(items: MutableList<RollingRelayRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingRelayRequestEngine(): RollingRelayRequestEngine {
    return RollingRelayRequestEngine(
        steps = mutableListOf<RollingRelayRequestAdjuster>(
            RollingRelayRequestFloorValueOffsetAdjuster(0.84),
            RoundingRollingRelayRequestAdjuster(0.25),
            TieredRollingRelayRequestAdjuster(
                mutableListOf<Double>(1253.0, 2639.0, 8752.0, 8754.0),
                mutableListOf<Double>(1.13, 0.84, 1.08, 1.15),
            ),
            LabelRollingRelayRequestAdjuster(mutableMapOf<String, Double>("online" to 1.16, "south" to 1.15)),
            PercentRollingRelayRequestAdjuster(3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 20305.5,
    )
}
