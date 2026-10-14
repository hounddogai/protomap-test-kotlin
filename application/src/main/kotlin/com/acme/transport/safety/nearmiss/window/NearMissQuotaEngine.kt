package com.acme.transport.safety.nearmiss.window

/** One step of the near miss quota calculation. */
interface NearMissQuotaModifier {
    fun describe(): String
    fun adjust(item: NearMissQuota, value: Double): Double
}

data class LabelNearMissQuotaAdjuster(var factors: MutableMap<String, Double>) : NearMissQuotaModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: NearMissQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentNearMissQuotaAdjuster(var percent: Double) : NearMissQuotaModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapNearMissQuotaAdjuster(var cap: Double) : NearMissQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumNearMissQuotaAdjuster(var minimum: Double) : NearMissQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the near miss quota steps in order and keeps a trace of each. */
data class NearMissQuotaEngine(
    var steps: MutableList<NearMissQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NearMissQuota): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NearMissQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<NearMissQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNearMissQuotaEngine(): NearMissQuotaEngine {
    return NearMissQuotaEngine(
        steps = mutableListOf<NearMissQuotaModifier>(
            LabelNearMissQuotaAdjuster(mutableMapOf<String, Double>("central" to 1.04, "online" to 1.11)),
            PercentNearMissQuotaAdjuster(-2.0),
            CapNearMissQuotaAdjuster(35214.0),
            MinimumNearMissQuotaAdjuster(16.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 44320.8,
    )
}
