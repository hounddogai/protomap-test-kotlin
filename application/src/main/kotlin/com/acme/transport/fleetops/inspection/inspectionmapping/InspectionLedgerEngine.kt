package com.acme.transport.fleetops.inspection.inspectionmapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the inspection ledger calculation. */
interface InspectionLedgerModifier {
    fun describe(): String
    fun adjust(item: InspectionLedger, value: Double): Double
}

data class TitleInspectionLedgerAdjuster(var factors: MutableMap<String, Double>) : InspectionLedgerModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: InspectionLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapInspectionLedgerAdjuster(var cap: Double) : InspectionLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionLedger, value: Double): Double = minOf(value, cap)
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

/** Runs the inspection ledger steps in order and keeps a trace of each. */
data class InspectionLedgerEngine(
    var steps: MutableList<InspectionLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionLedger): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionLedgerEngine(): InspectionLedgerEngine {
    return InspectionLedgerEngine(
        steps = mutableListOf<InspectionLedgerModifier>(
            TitleInspectionLedgerAdjuster(mutableMapOf<String, Double>("retail" to 1.38)),
            CapInspectionLedgerAdjuster(30715.0),
            TieredInspectionLedgerAdjuster(
                mutableListOf<Double>(2233.0, 4200.0),
                mutableListOf<Double>(0.88, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 30016.2,
    )
}
