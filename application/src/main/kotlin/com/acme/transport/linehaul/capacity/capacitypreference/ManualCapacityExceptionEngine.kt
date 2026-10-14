package com.acme.transport.linehaul.capacity.capacitypreference

/** One step of the manual capacity exception calculation. */
interface ManualCapacityExceptionStep {
    fun describe(): String
    fun adjust(item: ManualCapacityException, value: Double): Double
}

data class PercentManualCapacityExceptionAdjuster(var percent: Double) : ManualCapacityExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualCapacityException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualCapacityExceptionRevisionOffsetAdjuster(var weight: Double) : ManualCapacityExceptionStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: ManualCapacityException, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class MinimumManualCapacityExceptionAdjuster(var minimum: Double) : ManualCapacityExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualCapacityException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualCapacityExceptionAdjuster(var cap: Double) : ManualCapacityExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualCapacityException, value: Double): Double = minOf(value, cap)
}

/** Runs the manual capacity exception steps in order and keeps a trace of each. */
data class ManualCapacityExceptionEngine(
    var steps: MutableList<ManualCapacityExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualCapacityException): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualCapacityExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualCapacityException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualCapacityExceptionEngine(): ManualCapacityExceptionEngine {
    return ManualCapacityExceptionEngine(
        steps = mutableListOf<ManualCapacityExceptionStep>(
            PercentManualCapacityExceptionAdjuster(-19.3),
            ManualCapacityExceptionRevisionOffsetAdjuster(0.29),
            MinimumManualCapacityExceptionAdjuster(29.9),
            CapManualCapacityExceptionAdjuster(33210.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 83752.1,
    )
}
