package com.acme.transport.freightpay.audit.recorditem

/** One step of the lenient audit estimate calculation. */
interface LenientAuditEstimateFactor {
    fun describe(): String
    fun adjust(item: LenientAuditEstimate, value: Double): Double
}

data class PercentLenientAuditEstimateAdjuster(var percent: Double) : LenientAuditEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientAuditEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LabelLenientAuditEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientAuditEstimateFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientAuditEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientAuditEstimateAdjuster(var minimum: Double) : LenientAuditEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientAuditEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient audit estimate steps in order and keeps a trace of each. */
data class LenientAuditEstimateEngine(
    var steps: MutableList<LenientAuditEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientAuditEstimate): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientAuditEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientAuditEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientAuditEstimateEngine(): LenientAuditEstimateEngine {
    return LenientAuditEstimateEngine(
        steps = mutableListOf<LenientAuditEstimateFactor>(
            PercentLenientAuditEstimateAdjuster(21.6),
            LabelLenientAuditEstimateAdjuster(mutableMapOf<String, Double>("west" to 0.89, "north" to 1.12)),
            MinimumLenientAuditEstimateAdjuster(41.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 21414.6,
    )
}
