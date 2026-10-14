package com.acme.transport.network.node.bundle

/** One step of the scheduled node profile calculation. */
interface ScheduledNodeProfileStep {
    fun describe(): String
    fun adjust(item: ScheduledNodeProfile, value: Double): Double
}

data class MinimumScheduledNodeProfileAdjuster(var minimum: Double) : ScheduledNodeProfileStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledNodeProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledNodeProfileAdjuster(var cap: Double) : ScheduledNodeProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledNodeProfile, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledNodeProfileAdjuster(var percent: Double) : ScheduledNodeProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledNodeProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled node profile steps in order and keeps a trace of each. */
data class ScheduledNodeProfileEngine(
    var steps: MutableList<ScheduledNodeProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledNodeProfile): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledNodeProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledNodeProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledNodeProfileEngine(): ScheduledNodeProfileEngine {
    return ScheduledNodeProfileEngine(
        steps = mutableListOf<ScheduledNodeProfileStep>(
            MinimumScheduledNodeProfileAdjuster(3.1),
            CapScheduledNodeProfileAdjuster(13714.0),
            PercentScheduledNodeProfileAdjuster(18.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 57907.2,
    )
}
