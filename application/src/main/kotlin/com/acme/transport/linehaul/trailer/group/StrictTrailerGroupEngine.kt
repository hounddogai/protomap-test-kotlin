package com.acme.transport.linehaul.trailer.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict trailer group calculation. */
interface StrictTrailerGroupAdjuster {
    fun describe(): String
    fun adjust(item: StrictTrailerGroup, value: Double): Double
}

data class RoundingStrictTrailerGroupAdjuster(var step: Double) : StrictTrailerGroupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTrailerGroup, value: Double): Double = roundTo(value, step)
}

data class NameStrictTrailerGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictTrailerGroupAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: StrictTrailerGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentStrictTrailerGroupAdjuster(var percent: Double) : StrictTrailerGroupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTrailerGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictTrailerGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTrailerGroupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTrailerGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the strict trailer group steps in order and keeps a trace of each. */
data class StrictTrailerGroupEngine(
    var steps: MutableList<StrictTrailerGroupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictTrailerGroup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTrailerGroupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTrailerGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictTrailerGroupEngine(flags: Registry): StrictTrailerGroupEngine {
    return StrictTrailerGroupEngine(
        steps = mutableListOf<StrictTrailerGroupAdjuster>(
            RoundingStrictTrailerGroupAdjuster(5.0),
            NameStrictTrailerGroupAdjuster(
                mutableMapOf<String, Double>("central" to 1.02, "online" to 0.96, "north" to 1.0),
            ),
            PercentStrictTrailerGroupAdjuster(10.4),
            FlaggedStrictTrailerGroupAdjuster(flags, "strict-trailer-group-pending", 5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 70154.2,
    )
}
