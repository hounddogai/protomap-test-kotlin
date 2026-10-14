package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive camera event record calculation. */
interface AdaptiveCameraEventRecordModifier {
    fun describe(): String
    fun adjust(item: AdaptiveCameraEventRecord, value: Double): Double
}

data class RoundingAdaptiveCameraEventRecordAdjuster(var step: Double) : AdaptiveCameraEventRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveCameraEventRecord, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveCameraEventRecordAdjuster(var percent: Double) : AdaptiveCameraEventRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveCameraEventRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedAdaptiveCameraEventRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveCameraEventRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveCameraEventRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive camera event record steps in order and keeps a trace of each. */
data class AdaptiveCameraEventRecordEngine(
    var steps: MutableList<AdaptiveCameraEventRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveCameraEventRecord): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCameraEventRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCameraEventRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveCameraEventRecordEngine(flags: Registry): AdaptiveCameraEventRecordEngine {
    return AdaptiveCameraEventRecordEngine(
        steps = mutableListOf<AdaptiveCameraEventRecordModifier>(
            RoundingAdaptiveCameraEventRecordAdjuster(1.0),
            PercentAdaptiveCameraEventRecordAdjuster(23.6),
            FlaggedAdaptiveCameraEventRecordAdjuster(flags, "adaptive-camera-event-record-default", -6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 23193.8,
    )
}
