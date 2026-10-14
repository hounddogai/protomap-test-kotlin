package com.acme.transport.customs.broker.budget

/** One step of the broker slot calculation. */
interface BrokerSlotAdjuster {
    fun describe(): String
    fun adjust(item: BrokerSlot, value: Double): Double
}

data class BrokerSlotCreatedAtOffsetAdjuster(var weight: Double) : BrokerSlotAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: BrokerSlot, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class PercentBrokerSlotAdjuster(var percent: Double) : BrokerSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneBrokerSlotAdjuster(var factors: MutableMap<String, Double>) : BrokerSlotAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: BrokerSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the broker slot steps in order and keeps a trace of each. */
data class BrokerSlotEngine(
    var steps: MutableList<BrokerSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerSlot): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerSlotEngine(): BrokerSlotEngine {
    return BrokerSlotEngine(
        steps = mutableListOf<BrokerSlotAdjuster>(
            BrokerSlotCreatedAtOffsetAdjuster(0.8),
            PercentBrokerSlotAdjuster(-19.2),
            LaneBrokerSlotAdjuster(mutableMapOf<String, Double>("online" to 1.21, "retail" to 0.78)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 69839.6,
    )
}
