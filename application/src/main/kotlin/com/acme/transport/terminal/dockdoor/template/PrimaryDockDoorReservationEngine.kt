package com.acme.transport.terminal.dockdoor.template

/** One step of the primary dock door reservation calculation. */
interface PrimaryDockDoorReservationModifier {
    fun describe(): String
    fun adjust(item: PrimaryDockDoorReservation, value: Double): Double
}

data class PercentPrimaryDockDoorReservationAdjuster(var percent: Double) : PrimaryDockDoorReservationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryDockDoorReservation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SegmentPrimaryDockDoorReservationAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryDockDoorReservationModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: PrimaryDockDoorReservation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPrimaryDockDoorReservationAdjuster(var cap: Double) : PrimaryDockDoorReservationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryDockDoorReservation, value: Double): Double = minOf(value, cap)
}

/** Runs the primary dock door reservation steps in order and keeps a trace of each. */
data class PrimaryDockDoorReservationEngine(
    var steps: MutableList<PrimaryDockDoorReservationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryDockDoorReservation): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryDockDoorReservationModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryDockDoorReservation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryDockDoorReservationEngine(): PrimaryDockDoorReservationEngine {
    return PrimaryDockDoorReservationEngine(
        steps = mutableListOf<PrimaryDockDoorReservationModifier>(
            PercentPrimaryDockDoorReservationAdjuster(-19.3),
            SegmentPrimaryDockDoorReservationAdjuster(mutableMapOf<String, Double>("central" to 1.27)),
            CapPrimaryDockDoorReservationAdjuster(10357.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 25607.6,
    )
}
