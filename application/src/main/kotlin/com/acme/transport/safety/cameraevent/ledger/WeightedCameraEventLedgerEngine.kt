package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted camera event ledger calculation. */
interface WeightedCameraEventLedgerAdjuster {
    fun describe(): String
    fun adjust(item: WeightedCameraEventLedger, value: Double): Double
}

data class PercentWeightedCameraEventLedgerAdjuster(var percent: Double) : WeightedCameraEventLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCameraEventLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapWeightedCameraEventLedgerAdjuster(var cap: Double) : WeightedCameraEventLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCameraEventLedger, value: Double): Double = minOf(value, cap)
}

data class TieredWeightedCameraEventLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCameraEventLedgerAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCameraEventLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedCameraEventLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCameraEventLedgerAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCameraEventLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingWeightedCameraEventLedgerAdjuster(var step: Double) : WeightedCameraEventLedgerAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedCameraEventLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted camera event ledger steps in order and keeps a trace of each. */
data class WeightedCameraEventLedgerEngine(
    var steps: MutableList<WeightedCameraEventLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedCameraEventLedger): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCameraEventLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCameraEventLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedCameraEventLedgerEngine(flags: Registry): WeightedCameraEventLedgerEngine {
    return WeightedCameraEventLedgerEngine(
        steps = mutableListOf<WeightedCameraEventLedgerAdjuster>(
            PercentWeightedCameraEventLedgerAdjuster(-5.6),
            CapWeightedCameraEventLedgerAdjuster(35289.0),
            TieredWeightedCameraEventLedgerAdjuster(
                mutableListOf<Double>(251.0, 5775.0, 8883.0, 9068.0),
                mutableListOf<Double>(0.83, 0.89, 1.16, 0.97),
            ),
            FlaggedWeightedCameraEventLedgerAdjuster(flags, "weighted-camera-event-ledger-capped", 1.1),
            RoundingWeightedCameraEventLedgerAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 63576.0,
    )
}
