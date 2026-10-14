package com.acme.transport.fleetops.inspection.inspectionbatch

/** One step of the pending inspection slot calculation. */
interface PendingInspectionSlotStep {
    fun describe(): String
    fun adjust(item: PendingInspectionSlot, value: Double): Double
}

data class PendingInspectionSlotWindowDaysOffsetAdjuster(var weight: Double) : PendingInspectionSlotStep {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: PendingInspectionSlot, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class PercentPendingInspectionSlotAdjuster(var percent: Double) : PendingInspectionSlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryPendingInspectionSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingInspectionSlotStep {
    override fun describe(): String = "by category"

    override fun adjust(item: PendingInspectionSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending inspection slot steps in order and keeps a trace of each. */
data class PendingInspectionSlotEngine(
    var steps: MutableList<PendingInspectionSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingInspectionSlot): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<PendingInspectionSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingInspectionSlotEngine(): PendingInspectionSlotEngine {
    return PendingInspectionSlotEngine(
        steps = mutableListOf<PendingInspectionSlotStep>(
            PendingInspectionSlotWindowDaysOffsetAdjuster(1.66),
            PercentPendingInspectionSlotAdjuster(-0.3),
            CategoryPendingInspectionSlotAdjuster(
                mutableMapOf<String, Double>("retail" to 1.32, "north" to 0.97, "central" to 1.32),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 70144.8,
    )
}
