package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending idling profile calculation. */
interface PendingIdlingProfileStep {
    fun describe(): String
    fun adjust(item: PendingIdlingProfile, value: Double): Double
}

data class PercentPendingIdlingProfileAdjuster(var percent: Double) : PendingIdlingProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingIdlingProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingIdlingProfileAdjuster(var cap: Double) : PendingIdlingProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingIdlingProfile, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingIdlingProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingIdlingProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingIdlingProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NamePendingIdlingProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingIdlingProfileStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PendingIdlingProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingIdlingProfileAdjuster(var step: Double) : PendingIdlingProfileStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingIdlingProfile, value: Double): Double = roundTo(value, step)
}

/** Runs the pending idling profile steps in order and keeps a trace of each. */
data class PendingIdlingProfileEngine(
    var steps: MutableList<PendingIdlingProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingIdlingProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingIdlingProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingIdlingProfileEngine(flags: Registry): PendingIdlingProfileEngine {
    return PendingIdlingProfileEngine(
        steps = mutableListOf<PendingIdlingProfileStep>(
            PercentPendingIdlingProfileAdjuster(-8.9),
            CapPendingIdlingProfileAdjuster(39176.0),
            FlaggedPendingIdlingProfileAdjuster(flags, "pending-idling-profile-global", -5.7),
            NamePendingIdlingProfileAdjuster(
                mutableMapOf<String, Double>("south" to 0.89, "west" to 1.29, "east" to 1.38),
            ),
            RoundingPendingIdlingProfileAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 14238.4,
    )
}
