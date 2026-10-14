package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default accessorial cycle calculation. */
interface DefaultAccessorialCycleAdjuster {
    fun describe(): String
    fun adjust(item: DefaultAccessorialCycle, value: Double): Double
}

data class NameDefaultAccessorialCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultAccessorialCycleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultAccessorialCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedDefaultAccessorialCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultAccessorialCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultAccessorialCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefaultAccessorialCycleAdjuster(var step: Double) : DefaultAccessorialCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAccessorialCycle, value: Double): Double = roundTo(value, step)
}

data class CapDefaultAccessorialCycleAdjuster(var cap: Double) : DefaultAccessorialCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAccessorialCycle, value: Double): Double = minOf(value, cap)
}

data class DefaultAccessorialCycleVersionOffsetAdjuster(var weight: Double) : DefaultAccessorialCycleAdjuster {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: DefaultAccessorialCycle, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

/** Runs the default accessorial cycle steps in order and keeps a trace of each. */
data class DefaultAccessorialCycleEngine(
    var steps: MutableList<DefaultAccessorialCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultAccessorialCycle): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultAccessorialCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultAccessorialCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultAccessorialCycleEngine(flags: Registry): DefaultAccessorialCycleEngine {
    return DefaultAccessorialCycleEngine(
        steps = mutableListOf<DefaultAccessorialCycleAdjuster>(
            NameDefaultAccessorialCycleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.24, "online" to 0.96),
            ),
            FlaggedDefaultAccessorialCycleAdjuster(flags, "default-accessorial-cycle-adaptive", 7.3),
            RoundingDefaultAccessorialCycleAdjuster(0.05),
            CapDefaultAccessorialCycleAdjuster(32043.0),
            DefaultAccessorialCycleVersionOffsetAdjuster(1.36),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 11356.9,
    )
}
