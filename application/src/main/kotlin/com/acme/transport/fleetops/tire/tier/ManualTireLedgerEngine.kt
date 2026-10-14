package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual tire ledger calculation. */
interface ManualTireLedgerAdjuster {
    fun describe(): String
    fun adjust(item: ManualTireLedger, value: Double): Double
}

data class PercentManualTireLedgerAdjuster(var percent: Double) : ManualTireLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTireLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualTireLedgerAdjuster(var cap: Double) : ManualTireLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTireLedger, value: Double): Double = minOf(value, cap)
}

data class RoundingManualTireLedgerAdjuster(var step: Double) : ManualTireLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTireLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the manual tire ledger steps in order and keeps a trace of each. */
data class ManualTireLedgerEngine(
    var steps: MutableList<ManualTireLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualTireLedger): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTireLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualTireLedgerEngine(): ManualTireLedgerEngine {
    return ManualTireLedgerEngine(
        steps = mutableListOf<ManualTireLedgerAdjuster>(
            PercentManualTireLedgerAdjuster(-3.0),
            CapManualTireLedgerAdjuster(22509.0),
            RoundingManualTireLedgerAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 93806.3,
    )
}
