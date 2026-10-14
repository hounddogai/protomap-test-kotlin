package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection ledger calculation. */
interface InspectionLedgerModifier {
    fun describe(): String
    fun adjust(item: InspectionLedger, value: Double): Double
}

data class InspectionLedgerPriorityOffsetAdjuster(var weight: Double) : InspectionLedgerModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: InspectionLedger, value: Double): Double = value + item.priority.toDouble() * weight
}

data class CountryInspectionLedgerAdjuster(var factors: MutableMap<String, Double>) : InspectionLedgerModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: InspectionLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedInspectionLedgerAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionLedgerModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionLedger, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingInspectionLedgerAdjuster(var step: Double) : InspectionLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionLedger, value: Double): Double = roundTo(value, step)
}

/** Runs the inspection ledger steps in order and keeps a trace of each. */
data class InspectionLedgerEngine(
    var steps: MutableList<InspectionLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionLedger): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionLedgerModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionLedgerEngine(flags: Registry): InspectionLedgerEngine {
    return InspectionLedgerEngine(
        steps = mutableListOf<InspectionLedgerModifier>(
            InspectionLedgerPriorityOffsetAdjuster(0.93),
            CountryInspectionLedgerAdjuster(mutableMapOf<String, Double>("west" to 1.36)),
            TieredInspectionLedgerAdjuster(
                mutableListOf<Double>(796.0, 6098.0, 9266.0),
                mutableListOf<Double>(0.84, 0.97, 0.83),
            ),
            FlaggedInspectionLedgerAdjuster(flags, "inspection-ledger-fallback", -9.4),
            RoundingInspectionLedgerAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 90852.3,
    )
}
