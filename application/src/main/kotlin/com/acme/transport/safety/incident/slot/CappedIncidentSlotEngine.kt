package com.acme.transport.safety.incident.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped incident slot calculation. */
interface CappedIncidentSlotAdjuster {
    fun describe(): String
    fun adjust(item: CappedIncidentSlot, value: Double): Double
}

data class FlaggedCappedIncidentSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedIncidentSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedIncidentSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CappedIncidentSlotMarginOffsetAdjuster(var weight: Double) : CappedIncidentSlotAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: CappedIncidentSlot, value: Double): Double = value + item.margin * weight
}

data class RoundingCappedIncidentSlotAdjuster(var step: Double) : CappedIncidentSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedIncidentSlot, value: Double): Double = roundTo(value, step)
}

data class PercentCappedIncidentSlotAdjuster(var percent: Double) : CappedIncidentSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedIncidentSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped incident slot steps in order and keeps a trace of each. */
data class CappedIncidentSlotEngine(
    var steps: MutableList<CappedIncidentSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedIncidentSlot): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedIncidentSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedIncidentSlotEngine(flags: Registry): CappedIncidentSlotEngine {
    return CappedIncidentSlotEngine(
        steps = mutableListOf<CappedIncidentSlotAdjuster>(
            FlaggedCappedIncidentSlotAdjuster(flags, "capped-incident-slot-fallback", 5.0),
            CappedIncidentSlotMarginOffsetAdjuster(1.6),
            RoundingCappedIncidentSlotAdjuster(0.05),
            PercentCappedIncidentSlotAdjuster(-13.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 45115.3,
    )
}
