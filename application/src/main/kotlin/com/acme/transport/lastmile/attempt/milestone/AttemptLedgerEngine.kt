package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt ledger calculation. */
interface AttemptLedgerAdjuster {
    fun describe(): String
    fun adjust(item: AttemptLedger, value: Double): Double
}

data class ChannelAttemptLedgerAdjuster(var factors: MutableMap<String, Double>) : AttemptLedgerAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: AttemptLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAttemptLedgerAdjuster(var minimum: Double) : AttemptLedgerAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AttemptLedgerRevisionOffsetAdjuster(var weight: Double) : AttemptLedgerAdjuster {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: AttemptLedger, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the attempt ledger steps in order and keeps a trace of each. */
data class AttemptLedgerEngine(
    var steps: MutableList<AttemptLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptLedger): Double {
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

    fun evaluateAll(items: MutableList<AttemptLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptLedgerEngine(): AttemptLedgerEngine {
    return AttemptLedgerEngine(
        steps = mutableListOf<AttemptLedgerAdjuster>(
            ChannelAttemptLedgerAdjuster(
                mutableMapOf<String, Double>("north" to 0.98, "online" to 1.08, "east" to 1.23),
            ),
            MinimumAttemptLedgerAdjuster(45.4),
            AttemptLedgerRevisionOffsetAdjuster(1.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 15937.0,
    )
}
