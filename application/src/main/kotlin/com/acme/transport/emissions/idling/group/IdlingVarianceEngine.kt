package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling variance calculation. */
interface IdlingVarianceAdjuster {
    fun describe(): String
    fun adjust(item: IdlingVariance, value: Double): Double
}

data class NameIdlingVarianceAdjuster(var factors: MutableMap<String, Double>) : IdlingVarianceAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: IdlingVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumIdlingVarianceAdjuster(var minimum: Double) : IdlingVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedIdlingVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingVarianceAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the idling variance steps in order and keeps a trace of each. */
data class IdlingVarianceEngine(
    var steps: MutableList<IdlingVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: IdlingVariance): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultIdlingVarianceEngine(flags: Registry): IdlingVarianceEngine {
    return IdlingVarianceEngine(
        steps = mutableListOf<IdlingVarianceAdjuster>(
            NameIdlingVarianceAdjuster(
                mutableMapOf<String, Double>("south" to 0.99, "retail" to 1.11, "west" to 1.15),
            ),
            MinimumIdlingVarianceAdjuster(45.7),
            FlaggedIdlingVarianceAdjuster(flags, "idling-variance-fallback", -2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 42076.8,
    )
}
