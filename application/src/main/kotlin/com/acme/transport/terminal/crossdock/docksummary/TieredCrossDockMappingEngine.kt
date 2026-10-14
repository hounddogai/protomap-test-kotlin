package com.acme.transport.terminal.crossdock.docksummary

/** One step of the tiered cross dock mapping calculation. */
interface TieredCrossDockMappingModifier {
    fun describe(): String
    fun adjust(item: TieredCrossDockMapping, value: Double): Double
}

data class PercentTieredCrossDockMappingAdjuster(var percent: Double) : TieredCrossDockMappingModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredCrossDockMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class NameTieredCrossDockMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredCrossDockMappingModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredCrossDockMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredCrossDockMappingAdjuster(var minimum: Double) : TieredCrossDockMappingModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredCrossDockMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered cross dock mapping steps in order and keeps a trace of each. */
data class TieredCrossDockMappingEngine(
    var steps: MutableList<TieredCrossDockMappingModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TieredCrossDockMapping): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredCrossDockMappingModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredCrossDockMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTieredCrossDockMappingEngine(): TieredCrossDockMappingEngine {
    return TieredCrossDockMappingEngine(
        steps = mutableListOf<TieredCrossDockMappingModifier>(
            PercentTieredCrossDockMappingAdjuster(5.3),
            NameTieredCrossDockMappingAdjuster(mutableMapOf<String, Double>("south" to 1.31, "retail" to 0.78)),
            MinimumTieredCrossDockMappingAdjuster(35.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 9320.6,
    )
}
