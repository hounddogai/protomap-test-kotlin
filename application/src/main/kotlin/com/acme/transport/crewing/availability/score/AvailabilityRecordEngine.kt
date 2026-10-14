package com.acme.transport.crewing.availability.score

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability record calculation. */
interface AvailabilityRecordStep {
    fun describe(): String
    fun adjust(item: AvailabilityRecord, value: Double): Double
}

data class PercentAvailabilityRecordAdjuster(var percent: Double) : AvailabilityRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleAvailabilityRecordAdjuster(var factors: MutableMap<String, Double>) : AvailabilityRecordStep {
    override fun describe(): String = "by title"

    override fun adjust(item: AvailabilityRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingAvailabilityRecordAdjuster(var step: Double) : AvailabilityRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityRecord, value: Double): Double = roundTo(value, step)
}

data class CapAvailabilityRecordAdjuster(var cap: Double) : AvailabilityRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityRecord, value: Double): Double = minOf(value, cap)
}

/** Runs the availability record steps in order and keeps a trace of each. */
data class AvailabilityRecordEngine(
    var steps: MutableList<AvailabilityRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AvailabilityRecord): Double {
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

    fun evaluateAll(items: MutableList<AvailabilityRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAvailabilityRecordEngine(): AvailabilityRecordEngine {
    return AvailabilityRecordEngine(
        steps = mutableListOf<AvailabilityRecordStep>(
            PercentAvailabilityRecordAdjuster(7.9),
            TitleAvailabilityRecordAdjuster(
                mutableMapOf<String, Double>("online" to 0.94, "south" to 0.89, "north" to 0.75),
            ),
            RoundingAvailabilityRecordAdjuster(1.0),
            CapAvailabilityRecordAdjuster(49946.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 52956.3,
    )
}
