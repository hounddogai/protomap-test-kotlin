package com.acme.transport.customs.hold.strictholdaudit

/** One step of the strict hold audit calculation. */
interface StrictHoldAuditFactor {
    fun describe(): String
    fun adjust(item: StrictHoldAudit, value: Double): Double
}

data class MinimumStrictHoldAuditAdjuster(var minimum: Double) : StrictHoldAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictHoldAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentStrictHoldAuditAdjuster(var percent: Double) : StrictHoldAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictHoldAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictHoldAuditAdjuster(var cap: Double) : StrictHoldAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictHoldAudit, value: Double): Double = minOf(value, cap)
}

data class RegionStrictHoldAuditAdjuster(var factors: MutableMap<String, Double>) : StrictHoldAuditFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictHoldAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictHoldAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictHoldAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictHoldAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class StrictHoldAuditRevisionOffsetAdjuster(var weight: Double) : StrictHoldAuditFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: StrictHoldAudit, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the strict hold audit steps in order and keeps a trace of each. */
data class StrictHoldAuditEngine(
    var steps: MutableList<StrictHoldAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictHoldAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictHoldAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictHoldAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictHoldAuditEngine(): StrictHoldAuditEngine {
    return StrictHoldAuditEngine(
        steps = mutableListOf<StrictHoldAuditFactor>(
            MinimumStrictHoldAuditAdjuster(37.7),
            PercentStrictHoldAuditAdjuster(-3.8),
            CapStrictHoldAuditAdjuster(37456.0),
            RegionStrictHoldAuditAdjuster(mutableMapOf<String, Double>("retail" to 0.78)),
            TieredStrictHoldAuditAdjuster(
                mutableListOf<Double>(1618.0, 3263.0, 6565.0, 6856.0),
                mutableListOf<Double>(1.15, 0.98, 0.94, 0.96),
            ),
            StrictHoldAuditRevisionOffsetAdjuster(0.77),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 64513.7,
    )
}
