package com.acme.transport.freightpay.audit.rollup

/** One step of the tiered audit draft calculation. */
interface TieredAuditDraftAdjuster {
    fun describe(): String
    fun adjust(item: TieredAuditDraft, value: Double): Double
}

data class TieredTieredAuditDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredAuditDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredAuditDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredAuditDraftAdjuster(var percent: Double) : TieredAuditDraftAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredAuditDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAuditDraftFuelLitersOffsetAdjuster(var weight: Double) : TieredAuditDraftAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: TieredAuditDraft, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the tiered audit draft steps in order and keeps a trace of each. */
data class TieredAuditDraftEngine(
    var steps: MutableList<TieredAuditDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredAuditDraft): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredAuditDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredAuditDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredAuditDraftEngine(): TieredAuditDraftEngine {
    return TieredAuditDraftEngine(
        steps = mutableListOf<TieredAuditDraftAdjuster>(
            TieredTieredAuditDraftAdjuster(
                mutableListOf<Double>(835.0, 3897.0, 5869.0, 8399.0),
                mutableListOf<Double>(0.87, 1.14, 1.06, 1.08),
            ),
            PercentTieredAuditDraftAdjuster(12.6),
            TieredAuditDraftFuelLitersOffsetAdjuster(0.23),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 21154.6,
    )
}
