package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the accessorial slot calculation. */
interface AccessorialSlotAdjuster {
    fun describe(): String
    fun adjust(item: AccessorialSlot, value: Double): Double
}

data class FlaggedAccessorialSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AccessorialSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AccessorialSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAccessorialSlotAdjuster(var cap: Double) : AccessorialSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AccessorialSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingAccessorialSlotAdjuster(var step: Double) : AccessorialSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AccessorialSlot, value: Double): Double = roundTo(value, step)
}

/** Runs the accessorial slot steps in order and keeps a trace of each. */
data class AccessorialSlotEngine(
    var steps: MutableList<AccessorialSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AccessorialSlot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAccessorialSlotEngine(flags: Registry): AccessorialSlotEngine {
    return AccessorialSlotEngine(
        steps = mutableListOf<AccessorialSlotAdjuster>(
            FlaggedAccessorialSlotAdjuster(flags, "accessorial-slot-seasonal", -4.2),
            CapAccessorialSlotAdjuster(47559.0),
            RoundingAccessorialSlotAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 26461.0,
    )
}
