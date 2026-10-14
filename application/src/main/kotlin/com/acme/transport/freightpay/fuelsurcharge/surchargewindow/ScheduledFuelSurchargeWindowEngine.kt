package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

/** One step of the scheduled fuel surcharge window calculation. */
interface ScheduledFuelSurchargeWindowModifier {
    fun describe(): String
    fun adjust(item: ScheduledFuelSurchargeWindow, value: Double): Double
}

data class CapScheduledFuelSurchargeWindowAdjuster(var cap: Double) : ScheduledFuelSurchargeWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledFuelSurchargeWindow, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledFuelSurchargeWindowAdjuster(
    var minimum: Double,
) : ScheduledFuelSurchargeWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledFuelSurchargeWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameScheduledFuelSurchargeWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledFuelSurchargeWindowModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledFuelSurchargeWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled fuel surcharge window steps in order and keeps a trace of each. */
data class ScheduledFuelSurchargeWindowEngine(
    var steps: MutableList<ScheduledFuelSurchargeWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledFuelSurchargeWindow): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledFuelSurchargeWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledFuelSurchargeWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledFuelSurchargeWindowEngine(): ScheduledFuelSurchargeWindowEngine {
    return ScheduledFuelSurchargeWindowEngine(
        steps = mutableListOf<ScheduledFuelSurchargeWindowModifier>(
            CapScheduledFuelSurchargeWindowAdjuster(8435.0),
            MinimumScheduledFuelSurchargeWindowAdjuster(36.1),
            NameScheduledFuelSurchargeWindowAdjuster(
                mutableMapOf<String, Double>("west" to 1.18, "south" to 1.26),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 3492.3,
    )
}
