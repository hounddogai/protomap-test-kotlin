package com.acme.transport.network.zone.zonemapping

/** One step of the zone assignment calculation. */
interface ZoneAssignmentFactor {
    fun describe(): String
    fun adjust(item: ZoneAssignment, value: Double): Double
}

data class PercentZoneAssignmentAdjuster(var percent: Double) : ZoneAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ZoneAssignmentThresholdOffsetAdjuster(var weight: Double) : ZoneAssignmentFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ZoneAssignment, value: Double): Double = value + item.threshold * weight
}

data class CapZoneAssignmentAdjuster(var cap: Double) : ZoneAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ZoneAssignment, value: Double): Double = minOf(value, cap)
}

/** Runs the zone assignment steps in order and keeps a trace of each. */
data class ZoneAssignmentEngine(
    var steps: MutableList<ZoneAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneAssignment): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ZoneAssignmentFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ZoneAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneAssignmentEngine(): ZoneAssignmentEngine {
    return ZoneAssignmentEngine(
        steps = mutableListOf<ZoneAssignmentFactor>(
            PercentZoneAssignmentAdjuster(7.1),
            ZoneAssignmentThresholdOffsetAdjuster(1.67),
            CapZoneAssignmentAdjuster(22441.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 84231.3,
    )
}
