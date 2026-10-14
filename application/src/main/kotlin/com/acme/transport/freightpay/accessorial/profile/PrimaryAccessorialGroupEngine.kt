package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the primary accessorial group calculation. */
interface PrimaryAccessorialGroupAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryAccessorialGroup, value: Double): Double
}

data class FlaggedPrimaryAccessorialGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryAccessorialGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryAccessorialGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumPrimaryAccessorialGroupAdjuster(var minimum: Double) : PrimaryAccessorialGroupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAccessorialGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingPrimaryAccessorialGroupAdjuster(var step: Double) : PrimaryAccessorialGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryAccessorialGroup, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryAccessorialGroupAdjuster(var cap: Double) : PrimaryAccessorialGroupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryAccessorialGroup, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryAccessorialGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryAccessorialGroupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryAccessorialGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary accessorial group steps in order and keeps a trace of each. */
data class PrimaryAccessorialGroupEngine(
    var steps: MutableList<PrimaryAccessorialGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryAccessorialGroup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryAccessorialGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryAccessorialGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryAccessorialGroupEngine(flags: Registry): PrimaryAccessorialGroupEngine {
    return PrimaryAccessorialGroupEngine(
        steps = mutableListOf<PrimaryAccessorialGroupAdjuster>(
            FlaggedPrimaryAccessorialGroupAdjuster(flags, "primary-accessorial-group-rolling", 3.3),
            MinimumPrimaryAccessorialGroupAdjuster(8.4),
            RoundingPrimaryAccessorialGroupAdjuster(5.0),
            CapPrimaryAccessorialGroupAdjuster(34784.0),
            TieredPrimaryAccessorialGroupAdjuster(
                mutableListOf<Double>(2280.0, 3330.0, 4740.0, 9508.0),
                mutableListOf<Double>(1.18, 1.19, 1.11, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 5701.8,
    )
}
