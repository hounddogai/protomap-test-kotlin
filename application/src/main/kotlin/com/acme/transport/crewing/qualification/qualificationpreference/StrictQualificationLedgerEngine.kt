package com.acme.transport.crewing.qualification.qualificationpreference

/** One step of the strict qualification ledger calculation. */
interface StrictQualificationLedgerFactor {
    fun describe(): String
    fun adjust(item: StrictQualificationLedger, value: Double): Double
}

data class CapStrictQualificationLedgerAdjuster(var cap: Double) : StrictQualificationLedgerFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictQualificationLedger, value: Double): Double = minOf(value, cap)
}

data class SegmentStrictQualificationLedgerAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictQualificationLedgerFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: StrictQualificationLedger, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictQualificationLedgerAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictQualificationLedgerFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictQualificationLedger, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentStrictQualificationLedgerAdjuster(var percent: Double) : StrictQualificationLedgerFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictQualificationLedger, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict qualification ledger steps in order and keeps a trace of each. */
data class StrictQualificationLedgerEngine(
    var steps: MutableList<StrictQualificationLedgerFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictQualificationLedger): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictQualificationLedgerFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictQualificationLedger>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictQualificationLedgerEngine(): StrictQualificationLedgerEngine {
    return StrictQualificationLedgerEngine(
        steps = mutableListOf<StrictQualificationLedgerFactor>(
            CapStrictQualificationLedgerAdjuster(29952.0),
            SegmentStrictQualificationLedgerAdjuster(
                mutableMapOf<String, Double>("south" to 0.75, "retail" to 1.1),
            ),
            TieredStrictQualificationLedgerAdjuster(
                mutableListOf<Double>(7462.0, 8051.0),
                mutableListOf<Double>(0.98, 1.0),
            ),
            PercentStrictQualificationLedgerAdjuster(7.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 49648.3,
    )
}
