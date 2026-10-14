package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending backhaul override calculation. */
interface PendingBackhaulOverrideModifier {
    fun describe(): String
    fun adjust(item: PendingBackhaulOverride, value: Double): Double
}

data class PendingBackhaulOverrideRatioOffsetAdjuster(var weight: Double) : PendingBackhaulOverrideModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: PendingBackhaulOverride, value: Double): Double = value + item.ratio * weight
}

data class TieredPendingBackhaulOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingBackhaulOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingBackhaulOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingBackhaulOverrideAdjuster(var percent: Double) : PendingBackhaulOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingBackhaulOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending backhaul override steps in order and keeps a trace of each. */
data class PendingBackhaulOverrideEngine(
    var steps: MutableList<PendingBackhaulOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingBackhaulOverride): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingBackhaulOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingBackhaulOverrideEngine(): PendingBackhaulOverrideEngine {
    return PendingBackhaulOverrideEngine(
        steps = mutableListOf<PendingBackhaulOverrideModifier>(
            PendingBackhaulOverrideRatioOffsetAdjuster(1.98),
            TieredPendingBackhaulOverrideAdjuster(
                mutableListOf<Double>(1284.0, 5340.0),
                mutableListOf<Double>(1.17, 0.89),
            ),
            PercentPendingBackhaulOverrideAdjuster(8.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 90364.5,
    )
}
