package com.acme.transport.linehaul.relay.mapping

/** One step of the relay entry calculation. */
interface RelayEntryFactor {
    fun describe(): String
    fun adjust(item: RelayEntry, value: Double): Double
}

data class CapRelayEntryAdjuster(var cap: Double) : RelayEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayEntry, value: Double): Double = minOf(value, cap)
}

data class PercentRelayEntryAdjuster(var percent: Double) : RelayEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RelayEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RelayEntryBatchSizeOffsetAdjuster(var weight: Double) : RelayEntryFactor {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RelayEntry, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the relay entry steps in order and keeps a trace of each. */
data class RelayEntryEngine(
    var steps: MutableList<RelayEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RelayEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RelayEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayEntryEngine(): RelayEntryEngine {
    return RelayEntryEngine(
        steps = mutableListOf<RelayEntryFactor>(
            CapRelayEntryAdjuster(37636.0),
            PercentRelayEntryAdjuster(-7.2),
            RelayEntryBatchSizeOffsetAdjuster(1.69),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 66425.0,
    )
}
