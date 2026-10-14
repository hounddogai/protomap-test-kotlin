package com.acme.transport.network.node.manifest

/** One step of the node manifest calculation. */
interface NodeManifestStep {
    fun describe(): String
    fun adjust(item: NodeManifest, value: Double): Double
}

data class TitleNodeManifestAdjuster(var factors: MutableMap<String, Double>) : NodeManifestStep {
    override fun describe(): String = "by title"

    override fun adjust(item: NodeManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredNodeManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : NodeManifestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: NodeManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumNodeManifestAdjuster(var minimum: Double) : NodeManifestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NodeManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapNodeManifestAdjuster(var cap: Double) : NodeManifestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NodeManifest, value: Double): Double = minOf(value, cap)
}

/** Runs the node manifest steps in order and keeps a trace of each. */
data class NodeManifestEngine(
    var steps: MutableList<NodeManifestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: NodeManifest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: NodeManifestStep = steps[index]
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

    fun evaluateAll(items: MutableList<NodeManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultNodeManifestEngine(): NodeManifestEngine {
    return NodeManifestEngine(
        steps = mutableListOf<NodeManifestStep>(
            TitleNodeManifestAdjuster(mutableMapOf<String, Double>("north" to 1.1, "online" to 1.34)),
            TieredNodeManifestAdjuster(
                mutableListOf<Double>(1671.0, 4323.0, 6388.0),
                mutableListOf<Double>(0.84, 0.95, 1.2),
            ),
            MinimumNodeManifestAdjuster(4.1),
            CapNodeManifestAdjuster(13723.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 90680.8,
    )
}
