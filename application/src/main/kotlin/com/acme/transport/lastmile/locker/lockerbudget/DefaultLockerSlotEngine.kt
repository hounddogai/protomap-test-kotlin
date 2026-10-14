package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default locker slot calculation. */
interface DefaultLockerSlotModifier {
    fun describe(): String
    fun adjust(item: DefaultLockerSlot, value: Double): Double
}

data class PercentDefaultLockerSlotAdjuster(var percent: Double) : DefaultLockerSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultLockerSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DefaultLockerSlotQuantityOffsetAdjuster(var weight: Double) : DefaultLockerSlotModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DefaultLockerSlot, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingDefaultLockerSlotAdjuster(var step: Double) : DefaultLockerSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultLockerSlot, value: Double): Double = roundTo(value, step)
}

data class FlaggedDefaultLockerSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultLockerSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultLockerSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default locker slot steps in order and keeps a trace of each. */
data class DefaultLockerSlotEngine(
    var steps: MutableList<DefaultLockerSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultLockerSlot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultLockerSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultLockerSlotEngine(flags: Registry): DefaultLockerSlotEngine {
    return DefaultLockerSlotEngine(
        steps = mutableListOf<DefaultLockerSlotModifier>(
            PercentDefaultLockerSlotAdjuster(-10.2),
            DefaultLockerSlotQuantityOffsetAdjuster(1.45),
            RoundingDefaultLockerSlotAdjuster(0.1),
            FlaggedDefaultLockerSlotAdjuster(flags, "default-locker-slot-pending", -0.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 81741.0,
    )
}
