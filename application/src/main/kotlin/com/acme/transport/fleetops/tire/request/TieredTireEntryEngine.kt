package com.acme.transport.fleetops.tire.request

/** One step of the tiered tire entry calculation. */
interface TieredTireEntryStep {
    fun describe(): String
    fun adjust(item: TieredTireEntry, value: Double): Double
}

data class TieredTieredTireEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredTireEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredTireEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredTireEntrySequenceOffsetAdjuster(var weight: Double) : TieredTireEntryStep {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: TieredTireEntry, value: Double): Double = value + item.sequence.toDouble() * weight
}

data class CapTieredTireEntryAdjuster(var cap: Double) : TieredTireEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredTireEntry, value: Double): Double = minOf(value, cap)
}

data class MinimumTieredTireEntryAdjuster(var minimum: Double) : TieredTireEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredTireEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleTieredTireEntryAdjuster(var factors: MutableMap<String, Double>) : TieredTireEntryStep {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredTireEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered tire entry steps in order and keeps a trace of each. */
data class TieredTireEntryEngine(
    var steps: MutableList<TieredTireEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredTireEntry): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredTireEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<TieredTireEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredTireEntryEngine(): TieredTireEntryEngine {
    return TieredTireEntryEngine(
        steps = mutableListOf<TieredTireEntryStep>(
            TieredTieredTireEntryAdjuster(
                mutableListOf<Double>(1096.0, 9249.0, 9615.0),
                mutableListOf<Double>(0.81, 1.14, 1.02),
            ),
            TieredTireEntrySequenceOffsetAdjuster(1.51),
            CapTieredTireEntryAdjuster(28384.0),
            MinimumTieredTireEntryAdjuster(35.3),
            TitleTieredTireEntryAdjuster(
                mutableMapOf<String, Double>("west" to 0.89, "east" to 0.95, "south" to 1.32),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 35591.5,
    )
}
