package com.acme.transport.crewing.qualification.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the qualification ledger calculation. */
interface QualificationLedgerStep {
    fun describe(): String
    fun adjust(item: QualificationLedger, value: Double): Double
}

data class CapQualificationLedgerAdjuster(var cap: Double) : QualificationLedgerStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationLedger, value: Double): Double = minOf(value, cap)
}

data class MinimumQualificationLedgerAdjuster(var minimum: Double) : QualificationLedgerStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationLedger, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelQualificationLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationLedgerStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: QualificationLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the qualification ledger steps in order and keeps a trace of each. */
data class QualificationLedgerEngine(
    var steps: MutableList<QualificationLedgerStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: QualificationLedger): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultQualificationLedgerEngine(): QualificationLedgerEngine {
    return QualificationLedgerEngine(
        steps = mutableListOf<QualificationLedgerStep>(
            CapQualificationLedgerAdjuster(19576.0),
            MinimumQualificationLedgerAdjuster(36.9),
            ChannelQualificationLedgerAdjuster(
                mutableMapOf<String, Double>("central" to 1.01, "south" to 1.21, "online" to 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 31769.6,
    )
}
