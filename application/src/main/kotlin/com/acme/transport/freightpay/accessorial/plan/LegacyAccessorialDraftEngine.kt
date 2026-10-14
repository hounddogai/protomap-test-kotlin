package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy accessorial draft calculation. */
interface LegacyAccessorialDraftAdjuster {
    fun describe(): String
    fun adjust(item: LegacyAccessorialDraft, value: Double): Double
}

data class TitleLegacyAccessorialDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyAccessorialDraftAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyAccessorialDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyAccessorialDraftDurationMinutesOffsetAdjuster(
    var weight: Double,
) : LegacyAccessorialDraftAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: LegacyAccessorialDraft, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class MinimumLegacyAccessorialDraftAdjuster(var minimum: Double) : LegacyAccessorialDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyAccessorialDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyAccessorialDraftAdjuster(var percent: Double) : LegacyAccessorialDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyAccessorialDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy accessorial draft steps in order and keeps a trace of each. */
data class LegacyAccessorialDraftEngine(
    var steps: MutableList<LegacyAccessorialDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyAccessorialDraft): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyAccessorialDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyAccessorialDraftEngine(): LegacyAccessorialDraftEngine {
    return LegacyAccessorialDraftEngine(
        steps = mutableListOf<LegacyAccessorialDraftAdjuster>(
            TitleLegacyAccessorialDraftAdjuster(
                mutableMapOf<String, Double>("central" to 0.73, "north" to 1.13),
            ),
            LegacyAccessorialDraftDurationMinutesOffsetAdjuster(1.05),
            MinimumLegacyAccessorialDraftAdjuster(10.5),
            PercentLegacyAccessorialDraftAdjuster(-14.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 13153.7,
    )
}
