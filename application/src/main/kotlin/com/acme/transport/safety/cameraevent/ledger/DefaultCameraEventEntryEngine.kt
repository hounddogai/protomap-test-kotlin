package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default camera event entry calculation. */
interface DefaultCameraEventEntryModifier {
    fun describe(): String
    fun adjust(item: DefaultCameraEventEntry, value: Double): Double
}

data class TieredDefaultCameraEventEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCameraEventEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCameraEventEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultCameraEventEntryCeilingOffsetAdjuster(var weight: Double) : DefaultCameraEventEntryModifier {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: DefaultCameraEventEntry, value: Double): Double = value + item.ceiling * weight
}

data class PercentDefaultCameraEventEntryAdjuster(var percent: Double) : DefaultCameraEventEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCameraEventEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultCameraEventEntryAdjuster(var minimum: Double) : DefaultCameraEventEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCameraEventEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultCameraEventEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCameraEventEntryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCameraEventEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefaultCameraEventEntryAdjuster(var step: Double) : DefaultCameraEventEntryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCameraEventEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the default camera event entry steps in order and keeps a trace of each. */
data class DefaultCameraEventEntryEngine(
    var steps: MutableList<DefaultCameraEventEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultCameraEventEntry): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCameraEventEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCameraEventEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultCameraEventEntryEngine(flags: Registry): DefaultCameraEventEntryEngine {
    return DefaultCameraEventEntryEngine(
        steps = mutableListOf<DefaultCameraEventEntryModifier>(
            TieredDefaultCameraEventEntryAdjuster(
                mutableListOf<Double>(802.0, 2000.0, 5210.0, 5833.0),
                mutableListOf<Double>(1.18, 1.16, 0.83, 1.09),
            ),
            DefaultCameraEventEntryCeilingOffsetAdjuster(1.5),
            PercentDefaultCameraEventEntryAdjuster(5.4),
            MinimumDefaultCameraEventEntryAdjuster(7.5),
            FlaggedDefaultCameraEventEntryAdjuster(flags, "default-camera-event-entry-lenient", 2.5),
            RoundingDefaultCameraEventEntryAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 20525.6,
    )
}
