package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual camera event slot calculation. */
interface ManualCameraEventSlotStep {
    fun describe(): String
    fun adjust(item: ManualCameraEventSlot, value: Double): Double
}

data class CapManualCameraEventSlotAdjuster(var cap: Double) : ManualCameraEventSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCameraEventSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingManualCameraEventSlotAdjuster(var step: Double) : ManualCameraEventSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualCameraEventSlot, value: Double): Double = roundTo(value, step)
}

data class PercentManualCameraEventSlotAdjuster(var percent: Double) : ManualCameraEventSlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCameraEventSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual camera event slot steps in order and keeps a trace of each. */
data class ManualCameraEventSlotEngine(
    var steps: MutableList<ManualCameraEventSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualCameraEventSlot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualCameraEventSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualCameraEventSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualCameraEventSlotEngine(): ManualCameraEventSlotEngine {
    return ManualCameraEventSlotEngine(
        steps = mutableListOf<ManualCameraEventSlotStep>(
            CapManualCameraEventSlotAdjuster(10729.0),
            RoundingManualCameraEventSlotAdjuster(5.0),
            PercentManualCameraEventSlotAdjuster(3.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 10337.7,
    )
}
