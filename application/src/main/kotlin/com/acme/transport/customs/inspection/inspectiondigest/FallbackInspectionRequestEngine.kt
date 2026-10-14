package com.acme.transport.customs.inspection.inspectiondigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback inspection request calculation. */
interface FallbackInspectionRequestModifier {
    fun describe(): String
    fun adjust(item: FallbackInspectionRequest, value: Double): Double
}

data class RoundingFallbackInspectionRequestAdjuster(var step: Double) : FallbackInspectionRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackInspectionRequest, value: Double): Double = roundTo(value, step)
}

data class TieredFallbackInspectionRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackInspectionRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackInspectionRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackInspectionRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackInspectionRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackInspectionRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackInspectionRequestAdjuster(var minimum: Double) : FallbackInspectionRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackInspectionRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackInspectionRequestAdjuster(var cap: Double) : FallbackInspectionRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackInspectionRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback inspection request steps in order and keeps a trace of each. */
data class FallbackInspectionRequestEngine(
    var steps: MutableList<FallbackInspectionRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackInspectionRequest): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackInspectionRequestModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackInspectionRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackInspectionRequestEngine(flags: Registry): FallbackInspectionRequestEngine {
    return FallbackInspectionRequestEngine(
        steps = mutableListOf<FallbackInspectionRequestModifier>(
            RoundingFallbackInspectionRequestAdjuster(0.25),
            TieredFallbackInspectionRequestAdjuster(
                mutableListOf<Double>(1132.0, 4129.0, 9778.0),
                mutableListOf<Double>(0.8, 0.92, 1.09),
            ),
            FlaggedFallbackInspectionRequestAdjuster(flags, "fallback-inspection-request-legacy", 6.7),
            MinimumFallbackInspectionRequestAdjuster(40.7),
            CapFallbackInspectionRequestAdjuster(47156.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 35543.0,
    )
}
