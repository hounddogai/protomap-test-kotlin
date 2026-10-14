package com.acme.transport.lastmile.parcel.score

/** One step of the tiered parcel mapping calculation. */
interface TieredParcelMappingAdjuster {
    fun describe(): String
    fun adjust(item: TieredParcelMapping, value: Double): Double
}

data class PercentTieredParcelMappingAdjuster(var percent: Double) : TieredParcelMappingAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredParcelMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTieredParcelMappingAdjuster(var minimum: Double) : TieredParcelMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredParcelMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTieredParcelMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredParcelMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredParcelMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TieredParcelMappingCreatedAtOffsetAdjuster(var weight: Double) : TieredParcelMappingAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: TieredParcelMapping, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class NameTieredParcelMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredParcelMappingAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredParcelMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the tiered parcel mapping steps in order and keeps a trace of each. */
data class TieredParcelMappingEngine(
    var steps: MutableList<TieredParcelMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredParcelMapping): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredParcelMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredParcelMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredParcelMappingEngine(): TieredParcelMappingEngine {
    return TieredParcelMappingEngine(
        steps = mutableListOf<TieredParcelMappingAdjuster>(
            PercentTieredParcelMappingAdjuster(-9.4),
            MinimumTieredParcelMappingAdjuster(27.7),
            TieredTieredParcelMappingAdjuster(
                mutableListOf<Double>(4280.0, 5438.0, 7198.0),
                mutableListOf<Double>(1.04, 1.2, 1.09),
            ),
            TieredParcelMappingCreatedAtOffsetAdjuster(1.5),
            NameTieredParcelMappingAdjuster(
                mutableMapOf<String, Double>("north" to 0.95, "south" to 0.94, "retail" to 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 98794.1,
    )
}
