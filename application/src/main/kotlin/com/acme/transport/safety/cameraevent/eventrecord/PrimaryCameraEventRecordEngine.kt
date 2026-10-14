package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary camera event record calculation. */
interface PrimaryCameraEventRecordStep {
    fun describe(): String
    fun adjust(item: PrimaryCameraEventRecord, value: Double): Double
}

data class RoundingPrimaryCameraEventRecordAdjuster(var step: Double) : PrimaryCameraEventRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryCameraEventRecord, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryCameraEventRecordAdjuster(var cap: Double) : PrimaryCameraEventRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryCameraEventRecord, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryCameraEventRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryCameraEventRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryCameraEventRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary camera event record steps in order and keeps a trace of each. */
data class PrimaryCameraEventRecordEngine(
    var steps: MutableList<PrimaryCameraEventRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryCameraEventRecord): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryCameraEventRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryCameraEventRecordEngine(): PrimaryCameraEventRecordEngine {
    return PrimaryCameraEventRecordEngine(
        steps = mutableListOf<PrimaryCameraEventRecordStep>(
            RoundingPrimaryCameraEventRecordAdjuster(0.1),
            CapPrimaryCameraEventRecordAdjuster(12615.0),
            TieredPrimaryCameraEventRecordAdjuster(
                mutableListOf<Double>(1330.0, 4209.0, 6934.0),
                mutableListOf<Double>(0.98, 0.86, 0.98),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 70920.5,
    )
}
