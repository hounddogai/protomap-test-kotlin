package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary service area record calculation. */
interface PrimaryServiceAreaRecordModifier {
    fun describe(): String
    fun adjust(item: PrimaryServiceAreaRecord, value: Double): Double
}

data class FlaggedPrimaryServiceAreaRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServiceAreaRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServiceAreaRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPrimaryServiceAreaRecordAdjuster(var step: Double) : PrimaryServiceAreaRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryServiceAreaRecord, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryServiceAreaRecordAdjuster(var cap: Double) : PrimaryServiceAreaRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServiceAreaRecord, value: Double): Double = minOf(value, cap)
}

data class MinimumPrimaryServiceAreaRecordAdjuster(var minimum: Double) : PrimaryServiceAreaRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryServiceAreaRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the primary service area record steps in order and keeps a trace of each. */
data class PrimaryServiceAreaRecordEngine(
    var steps: MutableList<PrimaryServiceAreaRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryServiceAreaRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryServiceAreaRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryServiceAreaRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryServiceAreaRecordEngine(flags: Registry): PrimaryServiceAreaRecordEngine {
    return PrimaryServiceAreaRecordEngine(
        steps = mutableListOf<PrimaryServiceAreaRecordModifier>(
            FlaggedPrimaryServiceAreaRecordAdjuster(flags, "primary-service-area-record-legacy", -6.8),
            RoundingPrimaryServiceAreaRecordAdjuster(0.25),
            CapPrimaryServiceAreaRecordAdjuster(15610.0),
            MinimumPrimaryServiceAreaRecordAdjuster(42.7),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 38394.9,
    )
}
