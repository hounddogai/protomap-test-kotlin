package com.acme.transport.lastmile.locker.forecast

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default locker digest calculation. */
interface DefaultLockerDigestStep {
    fun describe(): String
    fun adjust(item: DefaultLockerDigest, value: Double): Double
}

data class MinimumDefaultLockerDigestAdjuster(var minimum: Double) : DefaultLockerDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultLockerDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultLockerDigestAdjuster(var percent: Double) : DefaultLockerDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultLockerDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultLockerDigestAdjuster(var step: Double) : DefaultLockerDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultLockerDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultLockerDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultLockerDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultLockerDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default locker digest steps in order and keeps a trace of each. */
data class DefaultLockerDigestEngine(
    var steps: MutableList<DefaultLockerDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultLockerDigest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultLockerDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultLockerDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultLockerDigestEngine(flags: Registry): DefaultLockerDigestEngine {
    return DefaultLockerDigestEngine(
        steps = mutableListOf<DefaultLockerDigestStep>(
            MinimumDefaultLockerDigestAdjuster(13.3),
            PercentDefaultLockerDigestAdjuster(-5.0),
            RoundingDefaultLockerDigestAdjuster(5.0),
            FlaggedDefaultLockerDigestAdjuster(flags, "default-locker-digest-tiered", -7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 65151.9,
    )
}
