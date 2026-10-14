package com.acme.transport.terminal.dwell.review

/** One step of the scheduled dwell template calculation. */
interface ScheduledDwellTemplateStep {
    fun describe(): String
    fun adjust(item: ScheduledDwellTemplate, value: Double): Double
}

data class CapScheduledDwellTemplateAdjuster(var cap: Double) : ScheduledDwellTemplateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDwellTemplate, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledDwellTemplateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDwellTemplateStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDwellTemplate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ScheduledDwellTemplateLoadKgOffsetAdjuster(var weight: Double) : ScheduledDwellTemplateStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ScheduledDwellTemplate, value: Double): Double = value + item.loadKg * weight
}

/** Runs the scheduled dwell template steps in order and keeps a trace of each. */
data class ScheduledDwellTemplateEngine(
    var steps: MutableList<ScheduledDwellTemplateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledDwellTemplate): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDwellTemplateStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDwellTemplate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledDwellTemplateEngine(): ScheduledDwellTemplateEngine {
    return ScheduledDwellTemplateEngine(
        steps = mutableListOf<ScheduledDwellTemplateStep>(
            CapScheduledDwellTemplateAdjuster(13353.0),
            TieredScheduledDwellTemplateAdjuster(
                mutableListOf<Double>(4005.0, 7851.0, 8599.0, 9324.0),
                mutableListOf<Double>(0.84, 1.03, 0.82, 1.04),
            ),
            ScheduledDwellTemplateLoadKgOffsetAdjuster(0.49),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 61857.3,
    )
}
