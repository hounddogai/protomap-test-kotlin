package com.acme.transport.emissions.idling.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled idling variance calculation. */
interface ScheduledIdlingVarianceModifier {
    fun describe(): String
    fun adjust(item: ScheduledIdlingVariance, value: Double): Double
}

data class ScheduledIdlingVarianceAttemptsOffsetAdjuster(var weight: Double) : ScheduledIdlingVarianceModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: ScheduledIdlingVariance, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class TieredScheduledIdlingVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledIdlingVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledIdlingVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledIdlingVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledIdlingVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledIdlingVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledIdlingVarianceAdjuster(var percent: Double) : ScheduledIdlingVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledIdlingVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class UnitScheduledIdlingVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledIdlingVarianceModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledIdlingVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled idling variance steps in order and keeps a trace of each. */
data class ScheduledIdlingVarianceEngine(
    var steps: MutableList<ScheduledIdlingVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledIdlingVariance): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledIdlingVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledIdlingVarianceEngine(flags: Registry): ScheduledIdlingVarianceEngine {
    return ScheduledIdlingVarianceEngine(
        steps = mutableListOf<ScheduledIdlingVarianceModifier>(
            ScheduledIdlingVarianceAttemptsOffsetAdjuster(1.07),
            TieredScheduledIdlingVarianceAdjuster(
                mutableListOf<Double>(434.0, 5071.0),
                mutableListOf<Double>(0.81, 0.84),
            ),
            FlaggedScheduledIdlingVarianceAdjuster(flags, "scheduled-idling-variance-manual", 6.3),
            PercentScheduledIdlingVarianceAdjuster(-11.3),
            UnitScheduledIdlingVarianceAdjuster(mutableMapOf<String, Double>("south" to 1.36, "east" to 0.86)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 86308.8,
    )
}
