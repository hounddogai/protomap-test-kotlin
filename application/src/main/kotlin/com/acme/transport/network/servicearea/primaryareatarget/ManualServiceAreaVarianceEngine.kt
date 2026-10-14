package com.acme.transport.network.servicearea.primaryareatarget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual service area variance calculation. */
interface ManualServiceAreaVarianceModifier {
    fun describe(): String
    fun adjust(item: ManualServiceAreaVariance, value: Double): Double
}

data class CapManualServiceAreaVarianceAdjuster(var cap: Double) : ManualServiceAreaVarianceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualServiceAreaVariance, value: Double): Double = minOf(value, cap)
}

data class PercentManualServiceAreaVarianceAdjuster(var percent: Double) : ManualServiceAreaVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualServiceAreaVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameManualServiceAreaVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualServiceAreaVarianceModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualServiceAreaVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual service area variance steps in order and keeps a trace of each. */
data class ManualServiceAreaVarianceEngine(
    var steps: MutableList<ManualServiceAreaVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualServiceAreaVariance): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualServiceAreaVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualServiceAreaVarianceEngine(): ManualServiceAreaVarianceEngine {
    return ManualServiceAreaVarianceEngine(
        steps = mutableListOf<ManualServiceAreaVarianceModifier>(
            CapManualServiceAreaVarianceAdjuster(33588.0),
            PercentManualServiceAreaVarianceAdjuster(-15.0),
            NameManualServiceAreaVarianceAdjuster(mutableMapOf<String, Double>("west" to 0.73)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 78824.4,
    )
}
