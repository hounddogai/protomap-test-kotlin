package com.acme.transport.crewing.qualification.schedule

/** One step of the tiered qualification ledger calculation. */
interface TieredQualificationLedgerAdjuster {
    fun describe(): String
    fun adjust(item: TieredQualificationLedger, value: Double): Double
}

data class TitleTieredQualificationLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredQualificationLedgerAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredQualificationLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredQualificationLedgerAdjuster(var percent: Double) : TieredQualificationLedgerAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredQualificationLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTieredQualificationLedgerAdjuster(var cap: Double) : TieredQualificationLedgerAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredQualificationLedger, value: Double): Double = minOf(value, cap)
}

/** Runs the tiered qualification ledger steps in order and keeps a trace of each. */
data class TieredQualificationLedgerEngine(
    var steps: MutableList<TieredQualificationLedgerAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredQualificationLedger): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredQualificationLedgerAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredQualificationLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredQualificationLedgerEngine(): TieredQualificationLedgerEngine {
    return TieredQualificationLedgerEngine(
        steps = mutableListOf<TieredQualificationLedgerAdjuster>(
            TitleTieredQualificationLedgerAdjuster(mutableMapOf<String, Double>("west" to 0.89)),
            PercentTieredQualificationLedgerAdjuster(16.2),
            CapTieredQualificationLedgerAdjuster(37712.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 94833.9,
    )
}
