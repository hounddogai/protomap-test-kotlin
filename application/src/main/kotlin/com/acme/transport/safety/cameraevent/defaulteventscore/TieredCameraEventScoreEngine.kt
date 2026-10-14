package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tiered camera event score calculation. */
interface TieredCameraEventScoreFactor {
    fun describe(): String
    fun adjust(item: TieredCameraEventScore, value: Double): Double
}

data class ChannelTieredCameraEventScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredCameraEventScoreFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: TieredCameraEventScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredCameraEventScoreAdjuster(var minimum: Double) : TieredCameraEventScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCameraEventScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTieredCameraEventScoreAdjuster(var step: Double) : TieredCameraEventScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TieredCameraEventScore, value: Double): Double = roundTo(value, step)
}

/** Runs the tiered camera event score steps in order and keeps a trace of each. */
data class TieredCameraEventScoreEngine(
    var steps: MutableList<TieredCameraEventScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredCameraEventScore): Double {
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

    fun evaluateAll(items: MutableList<TieredCameraEventScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredCameraEventScoreEngine(): TieredCameraEventScoreEngine {
    return TieredCameraEventScoreEngine(
        steps = mutableListOf<TieredCameraEventScoreFactor>(
            ChannelTieredCameraEventScoreAdjuster(mutableMapOf<String, Double>("north" to 0.84)),
            MinimumTieredCameraEventScoreAdjuster(3.7),
            RoundingTieredCameraEventScoreAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 57193.1,
    )
}
