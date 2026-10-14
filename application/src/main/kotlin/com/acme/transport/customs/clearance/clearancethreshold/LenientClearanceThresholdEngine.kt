package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient clearance threshold calculation. */
interface LenientClearanceThresholdModifier {
    fun describe(): String
    fun adjust(item: LenientClearanceThreshold, value: Double): Double
}

data class CapLenientClearanceThresholdAdjuster(var cap: Double) : LenientClearanceThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientClearanceThreshold, value: Double): Double = minOf(value, cap)
}

data class TieredLenientClearanceThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientClearanceThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientClearanceThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelLenientClearanceThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientClearanceThresholdModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientClearanceThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientClearanceThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientClearanceThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientClearanceThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient clearance threshold steps in order and keeps a trace of each. */
data class LenientClearanceThresholdEngine(
    var steps: MutableList<LenientClearanceThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientClearanceThreshold): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientClearanceThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientClearanceThresholdEngine(flags: Registry): LenientClearanceThresholdEngine {
    return LenientClearanceThresholdEngine(
        steps = mutableListOf<LenientClearanceThresholdModifier>(
            CapLenientClearanceThresholdAdjuster(14193.0),
            TieredLenientClearanceThresholdAdjuster(
                mutableListOf<Double>(1125.0, 2731.0, 9578.0, 9582.0),
                mutableListOf<Double>(1.06, 0.86, 1.19, 0.94),
            ),
            LabelLenientClearanceThresholdAdjuster(
                mutableMapOf<String, Double>("retail" to 1.34, "north" to 0.96),
            ),
            FlaggedLenientClearanceThresholdAdjuster(flags, "lenient-clearance-threshold-rolling", -4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 71198.0,
    )
}
