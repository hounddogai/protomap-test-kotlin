package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual ev charge segment calculation. */
interface ManualEvChargeSegmentFactor {
    fun describe(): String
    fun adjust(item: ManualEvChargeSegment, value: Double): Double
}

data class PercentManualEvChargeSegmentAdjuster(var percent: Double) : ManualEvChargeSegmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualEvChargeSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualEvChargeSegmentAdjuster(var cap: Double) : ManualEvChargeSegmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualEvChargeSegment, value: Double): Double = minOf(value, cap)
}

data class ManualEvChargeSegmentQuantityOffsetAdjuster(var weight: Double) : ManualEvChargeSegmentFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ManualEvChargeSegment, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

/** Runs the manual ev charge segment steps in order and keeps a trace of each. */
data class ManualEvChargeSegmentEngine(
    var steps: MutableList<ManualEvChargeSegmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualEvChargeSegment): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualEvChargeSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualEvChargeSegmentEngine(): ManualEvChargeSegmentEngine {
    return ManualEvChargeSegmentEngine(
        steps = mutableListOf<ManualEvChargeSegmentFactor>(
            PercentManualEvChargeSegmentAdjuster(-2.6),
            CapManualEvChargeSegmentAdjuster(46510.0),
            ManualEvChargeSegmentQuantityOffsetAdjuster(0.63),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 6929.5,
    )
}
