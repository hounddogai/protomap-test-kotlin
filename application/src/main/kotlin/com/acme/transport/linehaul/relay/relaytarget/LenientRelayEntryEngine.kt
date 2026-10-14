package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient relay entry calculation. */
interface LenientRelayEntryStep {
    fun describe(): String
    fun adjust(item: LenientRelayEntry, value: Double): Double
}

data class FlaggedLenientRelayEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientRelayEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientRelayEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientRelayEntryAdjuster(var minimum: Double) : LenientRelayEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRelayEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelLenientRelayEntryAdjuster(var factors: MutableMap<String, Double>) : LenientRelayEntryStep {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientRelayEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLenientRelayEntryAdjuster(var step: Double) : LenientRelayEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientRelayEntry, value: Double): Double = roundTo(value, step)
}

data class LenientRelayEntryAmountOffsetAdjuster(var weight: Double) : LenientRelayEntryStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: LenientRelayEntry, value: Double): Double = value + item.amount * weight
}

/** Runs the lenient relay entry steps in order and keeps a trace of each. */
data class LenientRelayEntryEngine(
    var steps: MutableList<LenientRelayEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientRelayEntry): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientRelayEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientRelayEntryEngine(flags: Registry): LenientRelayEntryEngine {
    return LenientRelayEntryEngine(
        steps = mutableListOf<LenientRelayEntryStep>(
            FlaggedLenientRelayEntryAdjuster(flags, "lenient-relay-entry-rolling", 3.8),
            MinimumLenientRelayEntryAdjuster(25.8),
            LabelLenientRelayEntryAdjuster(mutableMapOf<String, Double>("south" to 0.85, "retail" to 1.1)),
            RoundingLenientRelayEntryAdjuster(5.0),
            LenientRelayEntryAmountOffsetAdjuster(0.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 48989.4,
    )
}
