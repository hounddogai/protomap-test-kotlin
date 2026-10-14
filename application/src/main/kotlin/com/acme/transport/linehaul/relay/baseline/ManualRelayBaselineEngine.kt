package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual relay baseline calculation. */
interface ManualRelayBaselineModifier {
    fun describe(): String
    fun adjust(item: ManualRelayBaseline, value: Double): Double
}

data class RoundingManualRelayBaselineAdjuster(var step: Double) : ManualRelayBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualRelayBaseline, value: Double): Double = roundTo(value, step)
}

data class TieredManualRelayBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualRelayBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualRelayBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumManualRelayBaselineAdjuster(var minimum: Double) : ManualRelayBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualRelayBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualRelayBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualRelayBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualRelayBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the manual relay baseline steps in order and keeps a trace of each. */
data class ManualRelayBaselineEngine(
    var steps: MutableList<ManualRelayBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualRelayBaseline): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualRelayBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualRelayBaselineEngine(flags: Registry): ManualRelayBaselineEngine {
    return ManualRelayBaselineEngine(
        steps = mutableListOf<ManualRelayBaselineModifier>(
            RoundingManualRelayBaselineAdjuster(0.1),
            TieredManualRelayBaselineAdjuster(
                mutableListOf<Double>(5405.0, 5926.0, 6066.0, 8452.0),
                mutableListOf<Double>(0.8, 1.05, 1.02, 0.9),
            ),
            MinimumManualRelayBaselineAdjuster(47.0),
            FlaggedManualRelayBaselineAdjuster(flags, "manual-relay-baseline-legacy", 8.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 19738.5,
    )
}
