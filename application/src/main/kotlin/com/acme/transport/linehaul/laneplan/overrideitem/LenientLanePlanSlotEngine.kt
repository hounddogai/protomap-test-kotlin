package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient lane plan slot calculation. */
interface LenientLanePlanSlotAdjuster {
    fun describe(): String
    fun adjust(item: LenientLanePlanSlot, value: Double): Double
}

data class MinimumLenientLanePlanSlotAdjuster(var minimum: Double) : LenientLanePlanSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientLanePlanSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneLenientLanePlanSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientLanePlanSlotAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: LenientLanePlanSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLenientLanePlanSlotAdjuster(var step: Double) : LenientLanePlanSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientLanePlanSlot, value: Double): Double = roundTo(value, step)
}

data class LenientLanePlanSlotEndsAtOffsetAdjuster(var weight: Double) : LenientLanePlanSlotAdjuster {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LenientLanePlanSlot, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class PercentLenientLanePlanSlotAdjuster(var percent: Double) : LenientLanePlanSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientLanePlanSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient lane plan slot steps in order and keeps a trace of each. */
data class LenientLanePlanSlotEngine(
    var steps: MutableList<LenientLanePlanSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientLanePlanSlot): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientLanePlanSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientLanePlanSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientLanePlanSlotEngine(): LenientLanePlanSlotEngine {
    return LenientLanePlanSlotEngine(
        steps = mutableListOf<LenientLanePlanSlotAdjuster>(
            MinimumLenientLanePlanSlotAdjuster(13.5),
            LaneLenientLanePlanSlotAdjuster(mutableMapOf<String, Double>("east" to 1.23)),
            RoundingLenientLanePlanSlotAdjuster(5.0),
            LenientLanePlanSlotEndsAtOffsetAdjuster(0.13),
            PercentLenientLanePlanSlotAdjuster(11.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 38786.4,
    )
}
