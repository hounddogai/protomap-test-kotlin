package com.acme.transport.terminal.dwell.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual dwell estimate calculation. */
interface ManualDwellEstimateModifier {
    fun describe(): String
    fun adjust(item: ManualDwellEstimate, value: Double): Double
}

data class FlaggedManualDwellEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDwellEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDwellEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingManualDwellEstimateAdjuster(var step: Double) : ManualDwellEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualDwellEstimate, value: Double): Double = roundTo(value, step)
}

data class CapManualDwellEstimateAdjuster(var cap: Double) : ManualDwellEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualDwellEstimate, value: Double): Double = minOf(value, cap)
}

/** Runs the manual dwell estimate steps in order and keeps a trace of each. */
data class ManualDwellEstimateEngine(
    var steps: MutableList<ManualDwellEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualDwellEstimate): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualDwellEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualDwellEstimateEngine(flags: Registry): ManualDwellEstimateEngine {
    return ManualDwellEstimateEngine(
        steps = mutableListOf<ManualDwellEstimateModifier>(
            FlaggedManualDwellEstimateAdjuster(flags, "manual-dwell-estimate-primary", -5.5),
            RoundingManualDwellEstimateAdjuster(0.5),
            CapManualDwellEstimateAdjuster(11772.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 96979.0,
    )
}
