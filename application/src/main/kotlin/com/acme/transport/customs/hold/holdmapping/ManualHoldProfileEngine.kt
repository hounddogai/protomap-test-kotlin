package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual hold profile calculation. */
interface ManualHoldProfileAdjuster {
    fun describe(): String
    fun adjust(item: ManualHoldProfile, value: Double): Double
}

data class ManualHoldProfileMaximumOffsetAdjuster(var weight: Double) : ManualHoldProfileAdjuster {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ManualHoldProfile, value: Double): Double = value + item.maximum * weight
}

data class RoundingManualHoldProfileAdjuster(var step: Double) : ManualHoldProfileAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualHoldProfile, value: Double): Double = roundTo(value, step)
}

data class PercentManualHoldProfileAdjuster(var percent: Double) : ManualHoldProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualHoldProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual hold profile steps in order and keeps a trace of each. */
data class ManualHoldProfileEngine(
    var steps: MutableList<ManualHoldProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualHoldProfile): Double {
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

    fun evaluateAll(items: MutableList<ManualHoldProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualHoldProfileEngine(): ManualHoldProfileEngine {
    return ManualHoldProfileEngine(
        steps = mutableListOf<ManualHoldProfileAdjuster>(
            ManualHoldProfileMaximumOffsetAdjuster(0.49),
            RoundingManualHoldProfileAdjuster(1.0),
            PercentManualHoldProfileAdjuster(12.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 78638.9,
    )
}
