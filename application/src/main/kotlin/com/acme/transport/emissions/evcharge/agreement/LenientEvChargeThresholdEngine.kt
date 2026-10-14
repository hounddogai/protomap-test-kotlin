package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient ev charge threshold calculation. */
interface LenientEvChargeThresholdModifier {
    fun describe(): String
    fun adjust(item: LenientEvChargeThreshold, value: Double): Double
}

data class LocaleLenientEvChargeThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientEvChargeThresholdModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: LenientEvChargeThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LenientEvChargeThresholdMarginOffsetAdjuster(var weight: Double) : LenientEvChargeThresholdModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: LenientEvChargeThreshold, value: Double): Double = value + item.margin * weight
}

data class MinimumLenientEvChargeThresholdAdjuster(var minimum: Double) : LenientEvChargeThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientEvChargeThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientEvChargeThresholdAdjuster(var step: Double) : LenientEvChargeThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientEvChargeThreshold, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientEvChargeThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientEvChargeThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientEvChargeThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient ev charge threshold steps in order and keeps a trace of each. */
data class LenientEvChargeThresholdEngine(
    var steps: MutableList<LenientEvChargeThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientEvChargeThreshold): Double {
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

    fun evaluateAll(items: MutableList<LenientEvChargeThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientEvChargeThresholdEngine(flags: Registry): LenientEvChargeThresholdEngine {
    return LenientEvChargeThresholdEngine(
        steps = mutableListOf<LenientEvChargeThresholdModifier>(
            LocaleLenientEvChargeThresholdAdjuster(
                mutableMapOf<String, Double>("south" to 0.74, "online" to 0.81),
            ),
            LenientEvChargeThresholdMarginOffsetAdjuster(0.05),
            MinimumLenientEvChargeThresholdAdjuster(15.3),
            RoundingLenientEvChargeThresholdAdjuster(0.25),
            FlaggedLenientEvChargeThresholdAdjuster(flags, "lenient-ev-charge-threshold-strict", 9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 27130.0,
    )
}
