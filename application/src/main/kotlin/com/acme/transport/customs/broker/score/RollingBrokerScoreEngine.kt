package com.acme.transport.customs.broker.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling broker score calculation. */
interface RollingBrokerScoreAdjuster {
    fun describe(): String
    fun adjust(item: RollingBrokerScore, value: Double): Double
}

data class PercentRollingBrokerScoreAdjuster(var percent: Double) : RollingBrokerScoreAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingBrokerScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRollingBrokerScoreAdjuster(var step: Double) : RollingBrokerScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingBrokerScore, value: Double): Double = roundTo(value, step)
}

data class RollingBrokerScoreWindowDaysOffsetAdjuster(var weight: Double) : RollingBrokerScoreAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RollingBrokerScore, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class FlaggedRollingBrokerScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingBrokerScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingBrokerScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleRollingBrokerScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingBrokerScoreAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingBrokerScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rolling broker score steps in order and keeps a trace of each. */
data class RollingBrokerScoreEngine(
    var steps: MutableList<RollingBrokerScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingBrokerScore): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingBrokerScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingBrokerScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingBrokerScoreEngine(flags: Registry): RollingBrokerScoreEngine {
    return RollingBrokerScoreEngine(
        steps = mutableListOf<RollingBrokerScoreAdjuster>(
            PercentRollingBrokerScoreAdjuster(-23.2),
            RoundingRollingBrokerScoreAdjuster(5.0),
            RollingBrokerScoreWindowDaysOffsetAdjuster(1.86),
            FlaggedRollingBrokerScoreAdjuster(flags, "rolling-broker-score-pending", -7.3),
            TitleRollingBrokerScoreAdjuster(mutableMapOf<String, Double>("retail" to 1.04, "north" to 0.92)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 55306.9,
    )
}
