package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled near miss record calculation. */
interface ScheduledNearMissRecordStep {
    fun describe(): String
    fun adjust(item: ScheduledNearMissRecord, value: Double): Double
}

data class CapScheduledNearMissRecordAdjuster(var cap: Double) : ScheduledNearMissRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledNearMissRecord, value: Double): Double = minOf(value, cap)
}

data class RoundingScheduledNearMissRecordAdjuster(var step: Double) : ScheduledNearMissRecordStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledNearMissRecord, value: Double): Double = roundTo(value, step)
}

data class ScheduledNearMissRecordRateOffsetAdjuster(var weight: Double) : ScheduledNearMissRecordStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ScheduledNearMissRecord, value: Double): Double = value + item.rate * weight
}

data class FlaggedScheduledNearMissRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledNearMissRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledNearMissRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredScheduledNearMissRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledNearMissRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledNearMissRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled near miss record steps in order and keeps a trace of each. */
data class ScheduledNearMissRecordEngine(
    var steps: MutableList<ScheduledNearMissRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledNearMissRecord): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledNearMissRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledNearMissRecordEngine(flags: Registry): ScheduledNearMissRecordEngine {
    return ScheduledNearMissRecordEngine(
        steps = mutableListOf<ScheduledNearMissRecordStep>(
            CapScheduledNearMissRecordAdjuster(36325.0),
            RoundingScheduledNearMissRecordAdjuster(5.0),
            ScheduledNearMissRecordRateOffsetAdjuster(1.62),
            FlaggedScheduledNearMissRecordAdjuster(flags, "scheduled-near-miss-record-primary", -5.1),
            TieredScheduledNearMissRecordAdjuster(
                mutableListOf<Double>(2744.0, 3115.0, 3295.0, 5447.0),
                mutableListOf<Double>(0.84, 1.09, 1.12, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 11615.0,
    )
}
