package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled lane plan record calculation. */
interface ScheduledLanePlanRecordModifier {
    fun describe(): String
    fun adjust(item: ScheduledLanePlanRecord, value: Double): Double
}

data class RoundingScheduledLanePlanRecordAdjuster(var step: Double) : ScheduledLanePlanRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledLanePlanRecord, value: Double): Double = roundTo(value, step)
}

data class CapScheduledLanePlanRecordAdjuster(var cap: Double) : ScheduledLanePlanRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledLanePlanRecord, value: Double): Double = minOf(value, cap)
}

data class ScheduledLanePlanRecordCreatedAtOffsetAdjuster(
    var weight: Double,
) : ScheduledLanePlanRecordModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: ScheduledLanePlanRecord, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

/** Runs the scheduled lane plan record steps in order and keeps a trace of each. */
data class ScheduledLanePlanRecordEngine(
    var steps: MutableList<ScheduledLanePlanRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLanePlanRecord): Double {
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

    fun evaluateAll(items: MutableList<ScheduledLanePlanRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLanePlanRecordEngine(): ScheduledLanePlanRecordEngine {
    return ScheduledLanePlanRecordEngine(
        steps = mutableListOf<ScheduledLanePlanRecordModifier>(
            RoundingScheduledLanePlanRecordAdjuster(0.5),
            CapScheduledLanePlanRecordAdjuster(45728.0),
            ScheduledLanePlanRecordCreatedAtOffsetAdjuster(1.11),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 61293.5,
    )
}
