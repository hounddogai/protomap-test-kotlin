package com.acme.transport.customs.tariff.entry

/** One step of the tariff entry calculation. */
interface TariffEntryModifier {
    fun describe(): String
    fun adjust(item: TariffEntry, value: Double): Double
}

data class PercentTariffEntryAdjuster(var percent: Double) : TariffEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTariffEntryAdjuster(var cap: Double) : TariffEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffEntry, value: Double): Double = minOf(value, cap)
}

data class MinimumTariffEntryAdjuster(var minimum: Double) : TariffEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TariffEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tariff entry steps in order and keeps a trace of each. */
data class TariffEntryEngine(
    var steps: MutableList<TariffEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TariffEntry): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TariffEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTariffEntryEngine(): TariffEntryEngine {
    return TariffEntryEngine(
        steps = mutableListOf<TariffEntryModifier>(
            PercentTariffEntryAdjuster(-0.1),
            CapTariffEntryAdjuster(16491.0),
            MinimumTariffEntryAdjuster(45.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 40623.1,
    )
}
