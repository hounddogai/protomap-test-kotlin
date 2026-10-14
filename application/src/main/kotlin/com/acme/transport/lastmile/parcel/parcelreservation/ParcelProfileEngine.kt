package com.acme.transport.lastmile.parcel.parcelreservation

/** One step of the parcel profile calculation. */
interface ParcelProfileStep {
    fun describe(): String
    fun adjust(item: ParcelProfile, value: Double): Double
}

data class CapParcelProfileAdjuster(var cap: Double) : ParcelProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelProfile, value: Double): Double = minOf(value, cap)
}

data class NameParcelProfileAdjuster(var factors: MutableMap<String, Double>) : ParcelProfileStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ParcelProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentParcelProfileAdjuster(var percent: Double) : ParcelProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the parcel profile steps in order and keeps a trace of each. */
data class ParcelProfileEngine(
    var steps: MutableList<ParcelProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelProfile): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelProfileStep = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelProfileEngine(): ParcelProfileEngine {
    return ParcelProfileEngine(
        steps = mutableListOf<ParcelProfileStep>(
            CapParcelProfileAdjuster(5264.0),
            NameParcelProfileAdjuster(
                mutableMapOf<String, Double>("east" to 0.71, "west" to 1.03, "north" to 0.75),
            ),
            PercentParcelProfileAdjuster(17.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 56888.5,
    )
}
