package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped attempt record calculation. */
interface CappedAttemptRecordFactor {
    fun describe(): String
    fun adjust(item: CappedAttemptRecord, value: Double): Double
}

data class RoundingCappedAttemptRecordAdjuster(var step: Double) : CappedAttemptRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedAttemptRecord, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedAttemptRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedAttemptRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedAttemptRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneCappedAttemptRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedAttemptRecordFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedAttemptRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the capped attempt record steps in order and keeps a trace of each. */
data class CappedAttemptRecordEngine(
    var steps: MutableList<CappedAttemptRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedAttemptRecord): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedAttemptRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedAttemptRecordEngine(flags: Registry): CappedAttemptRecordEngine {
    return CappedAttemptRecordEngine(
        steps = mutableListOf<CappedAttemptRecordFactor>(
            RoundingCappedAttemptRecordAdjuster(5.0),
            FlaggedCappedAttemptRecordAdjuster(flags, "capped-attempt-record-tiered", -2.8),
            LaneCappedAttemptRecordAdjuster(
                mutableMapOf<String, Double>("east" to 1.33, "west" to 1.05, "central" to 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 55714.0,
    )
}
