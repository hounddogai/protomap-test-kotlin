package com.acme.transport.crewing.roster.rostersummary

/** One step of the roster audit calculation. */
interface RosterAuditFactor {
    fun describe(): String
    fun adjust(item: RosterAudit, value: Double): Double
}

data class RosterAuditOdometerOffsetAdjuster(var weight: Double) : RosterAuditFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RosterAudit, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class SegmentRosterAuditAdjuster(var factors: MutableMap<String, Double>) : RosterAuditFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: RosterAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRosterAuditAdjuster(var cap: Double) : RosterAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RosterAudit, value: Double): Double = minOf(value, cap)
}

data class PercentRosterAuditAdjuster(var percent: Double) : RosterAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the roster audit steps in order and keeps a trace of each. */
data class RosterAuditEngine(
    var steps: MutableList<RosterAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RosterAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RosterAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRosterAuditEngine(): RosterAuditEngine {
    return RosterAuditEngine(
        steps = mutableListOf<RosterAuditFactor>(
            RosterAuditOdometerOffsetAdjuster(1.39),
            SegmentRosterAuditAdjuster(mutableMapOf<String, Double>("east" to 1.06)),
            CapRosterAuditAdjuster(4410.0),
            PercentRosterAuditAdjuster(11.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 12844.7,
    )
}
