package com.acme.transport.network.servicearea.areaexception

/** One step of the capped service area mapping calculation. */
interface CappedServiceAreaMappingAdjuster {
    fun describe(): String
    fun adjust(item: CappedServiceAreaMapping, value: Double): Double
}

data class CappedServiceAreaMappingLoadKgOffsetAdjuster(var weight: Double) : CappedServiceAreaMappingAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CappedServiceAreaMapping, value: Double): Double = value + item.loadKg * weight
}

data class LabelCappedServiceAreaMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedServiceAreaMappingAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CappedServiceAreaMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedServiceAreaMappingAdjuster(var minimum: Double) : CappedServiceAreaMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedServiceAreaMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedServiceAreaMappingAdjuster(var percent: Double) : CappedServiceAreaMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedServiceAreaMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped service area mapping steps in order and keeps a trace of each. */
data class CappedServiceAreaMappingEngine(
    var steps: MutableList<CappedServiceAreaMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedServiceAreaMapping): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedServiceAreaMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CappedServiceAreaMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedServiceAreaMappingEngine(): CappedServiceAreaMappingEngine {
    return CappedServiceAreaMappingEngine(
        steps = mutableListOf<CappedServiceAreaMappingAdjuster>(
            CappedServiceAreaMappingLoadKgOffsetAdjuster(0.62),
            LabelCappedServiceAreaMappingAdjuster(
                mutableMapOf<String, Double>("retail" to 1.16, "online" to 1.09),
            ),
            MinimumCappedServiceAreaMappingAdjuster(3.2),
            PercentCappedServiceAreaMappingAdjuster(-1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 77495.7,
    )
}
