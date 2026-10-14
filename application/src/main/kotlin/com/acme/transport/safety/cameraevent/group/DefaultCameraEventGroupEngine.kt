package com.acme.transport.safety.cameraevent.group

/** One step of the default camera event group calculation. */
interface DefaultCameraEventGroupModifier {
    fun describe(): String
    fun adjust(item: DefaultCameraEventGroup, value: Double): Double
}

data class TieredDefaultCameraEventGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCameraEventGroupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCameraEventGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultCameraEventGroupAdjuster(var minimum: Double) : DefaultCameraEventGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCameraEventGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultCameraEventGroupAdjuster(var cap: Double) : DefaultCameraEventGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultCameraEventGroup, value: Double): Double = minOf(value, cap)
}

/** Runs the default camera event group steps in order and keeps a trace of each. */
data class DefaultCameraEventGroupEngine(
    var steps: MutableList<DefaultCameraEventGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultCameraEventGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCameraEventGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCameraEventGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultCameraEventGroupEngine(): DefaultCameraEventGroupEngine {
    return DefaultCameraEventGroupEngine(
        steps = mutableListOf<DefaultCameraEventGroupModifier>(
            TieredDefaultCameraEventGroupAdjuster(
                mutableListOf<Double>(2714.0, 5562.0, 7652.0),
                mutableListOf<Double>(0.89, 1.03, 1.16),
            ),
            MinimumDefaultCameraEventGroupAdjuster(31.3),
            CapDefaultCameraEventGroupAdjuster(22028.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 2855.5,
    )
}
