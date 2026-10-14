package com.acme.transport.fleetops.leasereturn.returnschedule

/** One step of the batched lease return rule calculation. */
interface BatchedLeaseReturnRuleModifier {
    fun describe(): String
    fun adjust(item: BatchedLeaseReturnRule, value: Double): Double
}

data class MinimumBatchedLeaseReturnRuleAdjuster(var minimum: Double) : BatchedLeaseReturnRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BatchedLeaseReturnRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentBatchedLeaseReturnRuleAdjuster(var percent: Double) : BatchedLeaseReturnRuleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BatchedLeaseReturnRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredBatchedLeaseReturnRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BatchedLeaseReturnRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BatchedLeaseReturnRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBatchedLeaseReturnRuleAdjuster(var cap: Double) : BatchedLeaseReturnRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BatchedLeaseReturnRule, value: Double): Double = minOf(value, cap)
}

data class TitleBatchedLeaseReturnRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : BatchedLeaseReturnRuleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: BatchedLeaseReturnRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the batched lease return rule steps in order and keeps a trace of each. */
data class BatchedLeaseReturnRuleEngine(
    var steps: MutableList<BatchedLeaseReturnRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BatchedLeaseReturnRule): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BatchedLeaseReturnRuleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BatchedLeaseReturnRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBatchedLeaseReturnRuleEngine(): BatchedLeaseReturnRuleEngine {
    return BatchedLeaseReturnRuleEngine(
        steps = mutableListOf<BatchedLeaseReturnRuleModifier>(
            MinimumBatchedLeaseReturnRuleAdjuster(30.3),
            PercentBatchedLeaseReturnRuleAdjuster(-20.0),
            TieredBatchedLeaseReturnRuleAdjuster(
                mutableListOf<Double>(14.0, 4519.0, 5376.0, 7064.0),
                mutableListOf<Double>(1.08, 1.1, 0.94, 0.88),
            ),
            CapBatchedLeaseReturnRuleAdjuster(10479.0),
            TitleBatchedLeaseReturnRuleAdjuster(
                mutableMapOf<String, Double>("east" to 1.37, "west" to 0.87, "south" to 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 27594.1,
    )
}
