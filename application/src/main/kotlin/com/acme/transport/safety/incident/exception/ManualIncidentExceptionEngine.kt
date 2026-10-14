package com.acme.transport.safety.incident.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual incident exception calculation. */
interface ManualIncidentExceptionAdjuster {
    fun describe(): String
    fun adjust(item: ManualIncidentException, value: Double): Double
}

data class TitleManualIncidentExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualIncidentExceptionAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualIncidentException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualIncidentExceptionAdjuster(var step: Double) : ManualIncidentExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualIncidentException, value: Double): Double = roundTo(value, step)
}

data class PercentManualIncidentExceptionAdjuster(var percent: Double) : ManualIncidentExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualIncidentException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualIncidentExceptionAdjuster(var cap: Double) : ManualIncidentExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualIncidentException, value: Double): Double = minOf(value, cap)
}

/** Runs the manual incident exception steps in order and keeps a trace of each. */
data class ManualIncidentExceptionEngine(
    var steps: MutableList<ManualIncidentExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualIncidentException): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualIncidentExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualIncidentException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualIncidentExceptionEngine(): ManualIncidentExceptionEngine {
    return ManualIncidentExceptionEngine(
        steps = mutableListOf<ManualIncidentExceptionAdjuster>(
            TitleManualIncidentExceptionAdjuster(
                mutableMapOf<String, Double>("east" to 1.38, "central" to 1.12, "south" to 0.88),
            ),
            RoundingManualIncidentExceptionAdjuster(5.0),
            PercentManualIncidentExceptionAdjuster(-14.1),
            CapManualIncidentExceptionAdjuster(17624.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 69189.0,
    )
}
