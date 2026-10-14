package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification slot calculation. */
interface QualificationSlotFactor {
    fun describe(): String
    fun adjust(item: QualificationSlot, value: Double): Double
}

data class PercentQualificationSlotAdjuster(var percent: Double) : QualificationSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingQualificationSlotAdjuster(var step: Double) : QualificationSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationSlot, value: Double): Double = roundTo(value, step)
}

data class CapQualificationSlotAdjuster(var cap: Double) : QualificationSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationSlot, value: Double): Double = minOf(value, cap)
}

data class TieredQualificationSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the qualification slot steps in order and keeps a trace of each. */
data class QualificationSlotEngine(
    var steps: MutableList<QualificationSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationSlot): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationSlotEngine(): QualificationSlotEngine {
    return QualificationSlotEngine(
        steps = mutableListOf<QualificationSlotFactor>(
            PercentQualificationSlotAdjuster(-3.1),
            RoundingQualificationSlotAdjuster(0.25),
            CapQualificationSlotAdjuster(37481.0),
            TieredQualificationSlotAdjuster(
                mutableListOf<Double>(401.0, 3059.0, 7967.0),
                mutableListOf<Double>(1.13, 0.94, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 51186.2,
    )
}
