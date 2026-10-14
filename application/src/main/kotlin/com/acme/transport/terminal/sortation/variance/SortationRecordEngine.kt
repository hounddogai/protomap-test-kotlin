package com.acme.transport.terminal.sortation.variance

/** One step of the sortation record calculation. */
interface SortationRecordModifier {
    fun describe(): String
    fun adjust(item: SortationRecord, value: Double): Double
}

data class CapSortationRecordAdjuster(var cap: Double) : SortationRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SortationRecord, value: Double): Double = minOf(value, cap)
}

data class TieredSortationRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSortationRecordAdjuster(var minimum: Double) : SortationRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SortationRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the sortation record steps in order and keeps a trace of each. */
data class SortationRecordEngine(
    var steps: MutableList<SortationRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationRecord): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SortationRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SortationRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationRecordEngine(): SortationRecordEngine {
    return SortationRecordEngine(
        steps = mutableListOf<SortationRecordModifier>(
            CapSortationRecordAdjuster(46201.0),
            TieredSortationRecordAdjuster(
                mutableListOf<Double>(6804.0, 8744.0),
                mutableListOf<Double>(1.12, 0.98),
            ),
            MinimumSortationRecordAdjuster(9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 4492.2,
    )
}
