package com.acme.transport.network.zone.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default zone cycle calculation. */
interface DefaultZoneCycleFactor {
    fun describe(): String
    fun adjust(item: DefaultZoneCycle, value: Double): Double
}

data class DefaultZoneCycleCeilingOffsetAdjuster(var weight: Double) : DefaultZoneCycleFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: DefaultZoneCycle, value: Double): Double = value + item.ceiling * weight
}

data class TieredDefaultZoneCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultZoneCycleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultZoneCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultZoneCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultZoneCycleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultZoneCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapDefaultZoneCycleAdjuster(var cap: Double) : DefaultZoneCycleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultZoneCycle, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultZoneCycleAdjuster(var step: Double) : DefaultZoneCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultZoneCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the default zone cycle steps in order and keeps a trace of each. */
data class DefaultZoneCycleEngineExtra(
    var steps: MutableList<DefaultZoneCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultZoneCycle): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultZoneCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultZoneCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultZoneCycleEngine(flags: Registry): DefaultZoneCycleEngineExtra {
    return DefaultZoneCycleEngineExtra(
        steps = mutableListOf<DefaultZoneCycleFactor>(
            DefaultZoneCycleCeilingOffsetAdjuster(0.58),
            TieredDefaultZoneCycleAdjuster(
                mutableListOf<Double>(1042.0, 9923.0),
                mutableListOf<Double>(1.07, 1.07),
            ),
            FlaggedDefaultZoneCycleAdjuster(flags, "default-zone-cycle-weighted", 3.6),
            CapDefaultZoneCycleAdjuster(13837.0),
            RoundingDefaultZoneCycleAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 26040.1,
    )
}
