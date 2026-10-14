package com.acme.transport.crewing.dutyperiod.perioddraft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary duty period draft calculation. */
interface PrimaryDutyPeriodDraftStep {
    fun describe(): String
    fun adjust(item: PrimaryDutyPeriodDraft, value: Double): Double
}

data class CapPrimaryDutyPeriodDraftAdjuster(var cap: Double) : PrimaryDutyPeriodDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDutyPeriodDraft, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryDutyPeriodDraftAdjuster(var step: Double) : PrimaryDutyPeriodDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryDutyPeriodDraft, value: Double): Double = roundTo(value, step)
}

data class LocalePrimaryDutyPeriodDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDutyPeriodDraftStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: PrimaryDutyPeriodDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPrimaryDutyPeriodDraftAdjuster(var minimum: Double) : PrimaryDutyPeriodDraftStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryDutyPeriodDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPrimaryDutyPeriodDraftAdjuster(var percent: Double) : PrimaryDutyPeriodDraftStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDutyPeriodDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the primary duty period draft steps in order and keeps a trace of each. */
data class PrimaryDutyPeriodDraftEngine(
    var steps: MutableList<PrimaryDutyPeriodDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryDutyPeriodDraft): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryDutyPeriodDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryDutyPeriodDraftEngine(): PrimaryDutyPeriodDraftEngine {
    return PrimaryDutyPeriodDraftEngine(
        steps = mutableListOf<PrimaryDutyPeriodDraftStep>(
            CapPrimaryDutyPeriodDraftAdjuster(38358.0),
            RoundingPrimaryDutyPeriodDraftAdjuster(0.1),
            LocalePrimaryDutyPeriodDraftAdjuster(
                mutableMapOf<String, Double>("south" to 1.14, "east" to 0.94, "central" to 1.08),
            ),
            MinimumPrimaryDutyPeriodDraftAdjuster(5.3),
            PercentPrimaryDutyPeriodDraftAdjuster(0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 56680.6,
    )
}
