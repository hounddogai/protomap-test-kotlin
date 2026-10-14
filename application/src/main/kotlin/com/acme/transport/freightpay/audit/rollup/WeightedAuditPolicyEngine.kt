package com.acme.transport.freightpay.audit.rollup

/** One step of the weighted audit policy calculation. */
interface WeightedAuditPolicyModifier {
    fun describe(): String
    fun adjust(item: WeightedAuditPolicy, value: Double): Double
}

data class WeightedAuditPolicyScoreOffsetAdjuster(var weight: Double) : WeightedAuditPolicyModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: WeightedAuditPolicy, value: Double): Double = value + item.score * weight
}

data class PercentWeightedAuditPolicyAdjuster(var percent: Double) : WeightedAuditPolicyModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAuditPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedAuditPolicyAdjuster(var minimum: Double) : WeightedAuditPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAuditPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted audit policy steps in order and keeps a trace of each. */
data class WeightedAuditPolicyEngine(
    var steps: MutableList<WeightedAuditPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedAuditPolicy): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAuditPolicyModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAuditPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedAuditPolicyEngine(): WeightedAuditPolicyEngine {
    return WeightedAuditPolicyEngine(
        steps = mutableListOf<WeightedAuditPolicyModifier>(
            WeightedAuditPolicyScoreOffsetAdjuster(0.76),
            PercentWeightedAuditPolicyAdjuster(-24.3),
            MinimumWeightedAuditPolicyAdjuster(16.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 73359.9,
    )
}
