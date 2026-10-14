package com.acme.transport.linehaul.laneplan.schedule

/** One step of the lane plan audit calculation. */
interface LanePlanAuditFactor {
    fun describe(): String
    fun adjust(item: LanePlanAudit, value: Double): Double
}

data class LanePlanAuditVersionOffsetAdjuster(var weight: Double) : LanePlanAuditFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: LanePlanAudit, value: Double): Double = value + item.version.toDouble() * weight
}

data class RegionLanePlanAuditAdjuster(var factors: MutableMap<String, Double>) : LanePlanAuditFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: LanePlanAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLanePlanAuditAdjuster(var percent: Double) : LanePlanAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLanePlanAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lane plan audit steps in order and keeps a trace of each. */
data class LanePlanAuditEngine(
    var steps: MutableList<LanePlanAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanAudit): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanAuditEngine(): LanePlanAuditEngine {
    return LanePlanAuditEngine(
        steps = mutableListOf<LanePlanAuditFactor>(
            LanePlanAuditVersionOffsetAdjuster(1.07),
            RegionLanePlanAuditAdjuster(mutableMapOf<String, Double>("west" to 0.7)),
            PercentLanePlanAuditAdjuster(-0.4),
            TieredLanePlanAuditAdjuster(mutableListOf<Double>(2743.0, 9446.0), mutableListOf<Double>(0.91, 0.9)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 51078.5,
    )
}
