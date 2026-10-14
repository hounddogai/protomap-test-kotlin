package com.acme.transport.network.transittime.timebatch

/** One step of the transit time audit calculation. */
interface TransitTimeAuditAdjuster {
    fun describe(): String
    fun adjust(item: TransitTimeAudit, value: Double): Double
}

data class TieredTransitTimeAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotTransitTimeAuditAdjuster(var factors: MutableMap<String, Double>) : TransitTimeAuditAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: TransitTimeAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTransitTimeAuditAdjuster(var percent: Double) : TransitTimeAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TransitTimeAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapTransitTimeAuditAdjuster(var cap: Double) : TransitTimeAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeAudit, value: Double): Double = minOf(value, cap)
}

data class MinimumTransitTimeAuditAdjuster(var minimum: Double) : TransitTimeAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TransitTimeAuditUpdatedAtOffsetAdjuster(var weight: Double) : TransitTimeAuditAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: TransitTimeAudit, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

/** Runs the transit time audit steps in order and keeps a trace of each. */
data class TransitTimeAuditEngine(
    var steps: MutableList<TransitTimeAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TransitTimeAudit): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTransitTimeAuditEngine(): TransitTimeAuditEngine {
    return TransitTimeAuditEngine(
        steps = mutableListOf<TransitTimeAuditAdjuster>(
            TieredTransitTimeAuditAdjuster(
                mutableListOf<Double>(6896.0, 7393.0),
                mutableListOf<Double>(0.92, 0.96),
            ),
            DepotTransitTimeAuditAdjuster(mutableMapOf<String, Double>("retail" to 1.13)),
            PercentTransitTimeAuditAdjuster(-6.1),
            CapTransitTimeAuditAdjuster(38071.0),
            MinimumTransitTimeAuditAdjuster(8.4),
            TransitTimeAuditUpdatedAtOffsetAdjuster(0.97),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 30335.1,
    )
}
