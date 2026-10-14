package com.acme.transport.linehaul.tender.assignment

/** One step of the manual tender snapshot calculation. */
interface ManualTenderSnapshotModifier {
    fun describe(): String
    fun adjust(item: ManualTenderSnapshot, value: Double): Double
}

data class LabelManualTenderSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualTenderSnapshotModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualTenderSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualTenderSnapshotPalletsOffsetAdjuster(var weight: Double) : ManualTenderSnapshotModifier {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: ManualTenderSnapshot, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class MinimumManualTenderSnapshotAdjuster(var minimum: Double) : ManualTenderSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTenderSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual tender snapshot steps in order and keeps a trace of each. */
data class ManualTenderSnapshotEngine(
    var steps: MutableList<ManualTenderSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualTenderSnapshot): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualTenderSnapshotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualTenderSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualTenderSnapshotEngine(): ManualTenderSnapshotEngine {
    return ManualTenderSnapshotEngine(
        steps = mutableListOf<ManualTenderSnapshotModifier>(
            LabelManualTenderSnapshotAdjuster(mutableMapOf<String, Double>("north" to 1.02, "online" to 0.88)),
            ManualTenderSnapshotPalletsOffsetAdjuster(0.44),
            MinimumManualTenderSnapshotAdjuster(7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 46042.4,
    )
}
