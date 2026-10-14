package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt template calculation. */
interface AttemptTemplateAdjuster {
    fun describe(): String
    fun adjust(item: AttemptTemplate, value: Double): Double
}

data class TieredAttemptTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptTemplateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleAttemptTemplateAdjuster(var factors: MutableMap<String, Double>) : AttemptTemplateAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: AttemptTemplate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAttemptTemplateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptTemplateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptTemplate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAttemptTemplateAdjuster(var step: Double) : AttemptTemplateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptTemplate, value: Double): Double = roundTo(value, step)
}

data class PercentAttemptTemplateAdjuster(var percent: Double) : AttemptTemplateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptTemplate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the attempt template steps in order and keeps a trace of each. */
data class AttemptTemplateEngine(
    var steps: MutableList<AttemptTemplateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptTemplate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptTemplateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptTemplateEngine(flags: Registry): AttemptTemplateEngine {
    return AttemptTemplateEngine(
        steps = mutableListOf<AttemptTemplateAdjuster>(
            TieredAttemptTemplateAdjuster(
                mutableListOf<Double>(1038.0, 1485.0, 1549.0, 6322.0),
                mutableListOf<Double>(1.12, 1.05, 1.13, 1.13),
            ),
            TitleAttemptTemplateAdjuster(mutableMapOf<String, Double>("north" to 1.09, "online" to 1.36)),
            FlaggedAttemptTemplateAdjuster(flags, "attempt-template-rolling", -7.2),
            RoundingAttemptTemplateAdjuster(0.5),
            PercentAttemptTemplateAdjuster(-11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 1175.4,
    )
}
