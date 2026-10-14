package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

/** One step of the regional fuel surcharge snapshot calculation. */
interface RegionalFuelSurchargeSnapshotModifier {
    fun describe(): String
    fun adjust(item: RegionalFuelSurchargeSnapshot, value: Double): Double
}

data class RegionalFuelSurchargeSnapshotThresholdOffsetAdjuster(
    var weight: Double,
) : RegionalFuelSurchargeSnapshotModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RegionalFuelSurchargeSnapshot, value: Double): Double = value + item.threshold * weight
}

data class PercentRegionalFuelSurchargeSnapshotAdjuster(
    var percent: Double,
) : RegionalFuelSurchargeSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalFuelSurchargeSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalFuelSurchargeSnapshotAdjuster(
    var minimum: Double,
) : RegionalFuelSurchargeSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalFuelSurchargeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneRegionalFuelSurchargeSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalFuelSurchargeSnapshotModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: RegionalFuelSurchargeSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRegionalFuelSurchargeSnapshotAdjuster(var cap: Double) : RegionalFuelSurchargeSnapshotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalFuelSurchargeSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the regional fuel surcharge snapshot steps in order and keeps a trace of each. */
data class RegionalFuelSurchargeSnapshotEngine(
    var steps: MutableList<RegionalFuelSurchargeSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalFuelSurchargeSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelSurchargeSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelSurchargeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalFuelSurchargeSnapshotEngine(): RegionalFuelSurchargeSnapshotEngine {
    return RegionalFuelSurchargeSnapshotEngine(
        steps = mutableListOf<RegionalFuelSurchargeSnapshotModifier>(
            RegionalFuelSurchargeSnapshotThresholdOffsetAdjuster(1.78),
            PercentRegionalFuelSurchargeSnapshotAdjuster(1.7),
            MinimumRegionalFuelSurchargeSnapshotAdjuster(15.7),
            LaneRegionalFuelSurchargeSnapshotAdjuster(
                mutableMapOf<String, Double>("west" to 1.03, "east" to 1.02, "online" to 1.25),
            ),
            CapRegionalFuelSurchargeSnapshotAdjuster(30235.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 81441.3,
    )
}
