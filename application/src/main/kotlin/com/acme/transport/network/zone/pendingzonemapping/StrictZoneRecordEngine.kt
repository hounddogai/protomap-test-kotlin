package com.acme.transport.network.zone.pendingzonemapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict zone record calculation. */
interface StrictZoneRecordStep {
    fun describe(): String
    fun adjust(item: StrictZoneRecord, value: Double): Double
}

data class PercentStrictZoneRecordAdjuster(var percent: Double) : StrictZoneRecordStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictZoneRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class StrictZoneRecordQuantityOffsetAdjuster(var weight: Double) : StrictZoneRecordStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: StrictZoneRecord, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class MinimumStrictZoneRecordAdjuster(var minimum: Double) : StrictZoneRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictZoneRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictZoneRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictZoneRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictZoneRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictZoneRecordAdjuster(var step: Double) : StrictZoneRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictZoneRecord, value: Double): Double = roundTo(value, step)
}

data class TitleStrictZoneRecordAdjuster(var factors: MutableMap<String, Double>) : StrictZoneRecordStep {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictZoneRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict zone record steps in order and keeps a trace of each. */
data class StrictZoneRecordEngine(
    var steps: MutableList<StrictZoneRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictZoneRecord): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictZoneRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictZoneRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictZoneRecordEngine(flags: Registry): StrictZoneRecordEngine {
    return StrictZoneRecordEngine(
        steps = mutableListOf<StrictZoneRecordStep>(
            PercentStrictZoneRecordAdjuster(8.1),
            StrictZoneRecordQuantityOffsetAdjuster(1.77),
            MinimumStrictZoneRecordAdjuster(8.4),
            FlaggedStrictZoneRecordAdjuster(flags, "strict-zone-record-scheduled", -9.1),
            RoundingStrictZoneRecordAdjuster(0.25),
            TitleStrictZoneRecordAdjuster(mutableMapOf<String, Double>("west" to 1.34, "south" to 1.26)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 69468.4,
    )
}
