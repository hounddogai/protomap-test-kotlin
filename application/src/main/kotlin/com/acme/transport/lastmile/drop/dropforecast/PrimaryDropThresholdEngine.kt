package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary drop threshold calculation. */
interface PrimaryDropThresholdAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryDropThreshold, value: Double): Double
}

data class PrimaryDropThresholdMarginOffsetAdjuster(var weight: Double) : PrimaryDropThresholdAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: PrimaryDropThreshold, value: Double): Double = value + item.margin * weight
}

data class NamePrimaryDropThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDropThresholdAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryDropThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPrimaryDropThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryDropThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryDropThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryDropThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryDropThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryDropThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary drop threshold steps in order and keeps a trace of each. */
data class PrimaryDropThresholdEngine(
    var steps: MutableList<PrimaryDropThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryDropThreshold): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryDropThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryDropThresholdEngine(flags: Registry): PrimaryDropThresholdEngine {
    return PrimaryDropThresholdEngine(
        steps = mutableListOf<PrimaryDropThresholdAdjuster>(
            PrimaryDropThresholdMarginOffsetAdjuster(0.15),
            NamePrimaryDropThresholdAdjuster(mutableMapOf<String, Double>("retail" to 0.85, "online" to 0.88)),
            FlaggedPrimaryDropThresholdAdjuster(flags, "primary-drop-threshold-manual", -9.3),
            TieredPrimaryDropThresholdAdjuster(
                mutableListOf<Double>(70.0, 870.0),
                mutableListOf<Double>(0.9, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 84125.3,
    )
}
