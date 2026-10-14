package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy inspection ledger calculation. */
interface LegacyInspectionLedgerModifier {
    fun describe(): String
    fun adjust(item: LegacyInspectionLedger, value: Double): Double
}

data class TieredLegacyInspectionLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyInspectionLedgerModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyInspectionLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyInspectionLedgerAdjuster(var step: Double) : LegacyInspectionLedgerModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyInspectionLedger, value: Double): Double = roundTo(value, step)
}

data class TitleLegacyInspectionLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyInspectionLedgerModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: LegacyInspectionLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLegacyInspectionLedgerAdjuster(var cap: Double) : LegacyInspectionLedgerModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyInspectionLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy inspection ledger steps in order and keeps a trace of each. */
data class LegacyInspectionLedgerEngine(
    var steps: MutableList<LegacyInspectionLedgerModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyInspectionLedger): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyInspectionLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyInspectionLedgerEngine(): LegacyInspectionLedgerEngine {
    return LegacyInspectionLedgerEngine(
        steps = mutableListOf<LegacyInspectionLedgerModifier>(
            TieredLegacyInspectionLedgerAdjuster(
                mutableListOf<Double>(540.0, 5443.0),
                mutableListOf<Double>(1.06, 0.8),
            ),
            RoundingLegacyInspectionLedgerAdjuster(0.5),
            TitleLegacyInspectionLedgerAdjuster(mutableMapOf<String, Double>("online" to 0.7)),
            CapLegacyInspectionLedgerAdjuster(13988.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 18750.9,
    )
}
