package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the attempt notice calculation. */
interface AttemptNoticeFactor {
    fun describe(): String
    fun adjust(item: AttemptNotice, value: Double): Double
}

data class RoundingAttemptNoticeAdjuster(var step: Double) : AttemptNoticeFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptNotice, value: Double): Double = roundTo(value, step)
}

data class CapAttemptNoticeAdjuster(var cap: Double) : AttemptNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptNotice, value: Double): Double = minOf(value, cap)
}

data class FlaggedAttemptNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptNoticeFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt notice steps in order and keeps a trace of each. */
data class AttemptNoticeEngine(
    var steps: MutableList<AttemptNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AttemptNotice): Double {
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

    fun evaluateAll(items: MutableList<AttemptNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAttemptNoticeEngine(flags: Registry): AttemptNoticeEngine {
    return AttemptNoticeEngine(
        steps = mutableListOf<AttemptNoticeFactor>(
            RoundingAttemptNoticeAdjuster(5.0),
            CapAttemptNoticeAdjuster(16278.0),
            FlaggedAttemptNoticeAdjuster(flags, "attempt-notice-lenient", 0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 94467.8,
    )
}
