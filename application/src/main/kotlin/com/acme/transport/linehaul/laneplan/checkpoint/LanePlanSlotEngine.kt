package com.acme.transport.linehaul.laneplan.checkpoint

/** One step of the lane plan slot calculation. */
interface LanePlanSlotFactor {
    fun describe(): String
    fun adjust(item: LanePlanSlot, value: Double): Double
}

data class CapLanePlanSlotAdjuster(var cap: Double) : LanePlanSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanSlot, value: Double): Double = minOf(value, cap)
}

data class LocaleLanePlanSlotAdjuster(var factors: MutableMap<String, Double>) : LanePlanSlotFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: LanePlanSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLanePlanSlotAdjuster(var percent: Double) : LanePlanSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lane plan slot steps in order and keeps a trace of each. */
data class LanePlanSlotEngine(
    var steps: MutableList<LanePlanSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LanePlanSlot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLanePlanSlotEngine(): LanePlanSlotEngine {
    return LanePlanSlotEngine(
        steps = mutableListOf<LanePlanSlotFactor>(
            CapLanePlanSlotAdjuster(13944.0),
            LocaleLanePlanSlotAdjuster(
                mutableMapOf<String, Double>("retail" to 1.25, "east" to 1.02, "west" to 1.34),
            ),
            PercentLanePlanSlotAdjuster(-12.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 49249.0,
    )
}
