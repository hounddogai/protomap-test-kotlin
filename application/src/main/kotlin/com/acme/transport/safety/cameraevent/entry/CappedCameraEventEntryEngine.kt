package com.acme.transport.safety.cameraevent.entry

/** One step of the capped camera event entry calculation. */
interface CappedCameraEventEntryFactor {
    fun describe(): String
    fun adjust(item: CappedCameraEventEntry, value: Double): Double
}

data class CapCappedCameraEventEntryAdjuster(var cap: Double) : CappedCameraEventEntryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedCameraEventEntry, value: Double): Double = minOf(value, cap)
}

data class PercentCappedCameraEventEntryAdjuster(var percent: Double) : CappedCameraEventEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedCameraEventEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceCappedCameraEventEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedCameraEventEntryFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: CappedCameraEventEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCappedCameraEventEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedCameraEventEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedCameraEventEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedCameraEventEntryAdjuster(var minimum: Double) : CappedCameraEventEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedCameraEventEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped camera event entry steps in order and keeps a trace of each. */
data class CappedCameraEventEntryEngine(
    var steps: MutableList<CappedCameraEventEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedCameraEventEntry): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedCameraEventEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedCameraEventEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedCameraEventEntryEngine(): CappedCameraEventEntryEngine {
    return CappedCameraEventEntryEngine(
        steps = mutableListOf<CappedCameraEventEntryFactor>(
            CapCappedCameraEventEntryAdjuster(21484.0),
            PercentCappedCameraEventEntryAdjuster(0.9),
            SourceCappedCameraEventEntryAdjuster(
                mutableMapOf<String, Double>("central" to 1.13, "online" to 1.28),
            ),
            TieredCappedCameraEventEntryAdjuster(
                mutableListOf<Double>(464.0, 2466.0, 6453.0, 9440.0),
                mutableListOf<Double>(1.12, 0.85, 1.17, 1.04),
            ),
            MinimumCappedCameraEventEntryAdjuster(41.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 6270.2,
    )
}
