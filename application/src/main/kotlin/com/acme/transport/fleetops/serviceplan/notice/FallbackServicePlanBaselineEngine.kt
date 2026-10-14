package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback service plan baseline calculation. */
interface FallbackServicePlanBaselineStep {
    fun describe(): String
    fun adjust(item: FallbackServicePlanBaseline, value: Double): Double
}

data class FlaggedFallbackServicePlanBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackServicePlanBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackServicePlanBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapFallbackServicePlanBaselineAdjuster(var cap: Double) : FallbackServicePlanBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackServicePlanBaseline, value: Double): Double = minOf(value, cap)
}

data class FallbackServicePlanBaselineWindowDaysOffsetAdjuster(
    var weight: Double,
) : FallbackServicePlanBaselineStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: FallbackServicePlanBaseline, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

/** Runs the fallback service plan baseline steps in order and keeps a trace of each. */
data class FallbackServicePlanBaselineEngine(
    var steps: MutableList<FallbackServicePlanBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackServicePlanBaseline): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackServicePlanBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackServicePlanBaselineEngine(flags: Registry): FallbackServicePlanBaselineEngine {
    return FallbackServicePlanBaselineEngine(
        steps = mutableListOf<FallbackServicePlanBaselineStep>(
            FlaggedFallbackServicePlanBaselineAdjuster(flags, "fallback-service-plan-baseline-lenient", 8.5),
            CapFallbackServicePlanBaselineAdjuster(6748.0),
            FallbackServicePlanBaselineWindowDaysOffsetAdjuster(0.63),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 90207.2,
    )
}
