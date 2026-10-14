package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled accessorial draft calculation. */
interface ScheduledAccessorialDraftFactor {
    fun describe(): String
    fun adjust(item: ScheduledAccessorialDraft, value: Double): Double
}

data class CapScheduledAccessorialDraftAdjuster(var cap: Double) : ScheduledAccessorialDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledAccessorialDraft, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledAccessorialDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledAccessorialDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledAccessorialDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledAccessorialDraftAdjuster(var step: Double) : ScheduledAccessorialDraftFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledAccessorialDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled accessorial draft steps in order and keeps a trace of each. */
data class ScheduledAccessorialDraftEngine(
    var steps: MutableList<ScheduledAccessorialDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledAccessorialDraft): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledAccessorialDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledAccessorialDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledAccessorialDraftEngine(flags: Registry): ScheduledAccessorialDraftEngine {
    return ScheduledAccessorialDraftEngine(
        steps = mutableListOf<ScheduledAccessorialDraftFactor>(
            CapScheduledAccessorialDraftAdjuster(40548.0),
            FlaggedScheduledAccessorialDraftAdjuster(flags, "scheduled-accessorial-draft-tiered", 5.7),
            RoundingScheduledAccessorialDraftAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 29912.0,
    )
}
