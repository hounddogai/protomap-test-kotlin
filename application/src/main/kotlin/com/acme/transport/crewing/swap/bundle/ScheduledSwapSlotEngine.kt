package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled swap slot calculation. */
interface ScheduledSwapSlotStep {
    fun describe(): String
    fun adjust(item: ScheduledSwapSlot, value: Double): Double
}

data class PercentScheduledSwapSlotAdjuster(var percent: Double) : ScheduledSwapSlotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledSwapSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedScheduledSwapSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledSwapSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledSwapSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingScheduledSwapSlotAdjuster(var step: Double) : ScheduledSwapSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledSwapSlot, value: Double): Double = roundTo(value, step)
}

data class NameScheduledSwapSlotAdjuster(var factors: MutableMap<String, Double>) : ScheduledSwapSlotStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledSwapSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScheduledSwapSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledSwapSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledSwapSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledSwapSlotAdjuster(var cap: Double) : ScheduledSwapSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledSwapSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled swap slot steps in order and keeps a trace of each. */
data class ScheduledSwapSlotEngine(
    var steps: MutableList<ScheduledSwapSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledSwapSlot): Double {
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

    fun evaluateAll(items: MutableList<ScheduledSwapSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledSwapSlotEngine(flags: Registry): ScheduledSwapSlotEngine {
    return ScheduledSwapSlotEngine(
        steps = mutableListOf<ScheduledSwapSlotStep>(
            PercentScheduledSwapSlotAdjuster(-18.0),
            FlaggedScheduledSwapSlotAdjuster(flags, "scheduled-swap-slot-capped", 6.8),
            RoundingScheduledSwapSlotAdjuster(1.0),
            NameScheduledSwapSlotAdjuster(mutableMapOf<String, Double>("east" to 0.75, "retail" to 0.85)),
            TieredScheduledSwapSlotAdjuster(
                mutableListOf<Double>(2584.0, 3283.0, 5019.0, 7730.0),
                mutableListOf<Double>(1.12, 0.97, 1.07, 1.14),
            ),
            CapScheduledSwapSlotAdjuster(9444.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 40496.2,
    )
}
