package com.acme.transport.fleetops.serviceplan.notice

/** One step of the service plan notice calculation. */
interface ServicePlanNoticeFactor {
    fun describe(): String
    fun adjust(item: ServicePlanNotice, value: Double): Double
}

data class LaneServicePlanNoticeAdjuster(var factors: MutableMap<String, Double>) : ServicePlanNoticeFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: ServicePlanNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapServicePlanNoticeAdjuster(var cap: Double) : ServicePlanNoticeFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ServicePlanNotice, value: Double): Double = minOf(value, cap)
}

data class ServicePlanNoticeMarginOffsetAdjuster(var weight: Double) : ServicePlanNoticeFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ServicePlanNotice, value: Double): Double = value + item.margin * weight
}

data class MinimumServicePlanNoticeAdjuster(var minimum: Double) : ServicePlanNoticeFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ServicePlanNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredServicePlanNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServicePlanNoticeFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServicePlanNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the service plan notice steps in order and keeps a trace of each. */
data class ServicePlanNoticeEngine(
    var steps: MutableList<ServicePlanNoticeFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServicePlanNotice): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ServicePlanNoticeFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ServicePlanNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServicePlanNoticeEngine(): ServicePlanNoticeEngine {
    return ServicePlanNoticeEngine(
        steps = mutableListOf<ServicePlanNoticeFactor>(
            LaneServicePlanNoticeAdjuster(mutableMapOf<String, Double>("south" to 1.16, "north" to 1.02)),
            CapServicePlanNoticeAdjuster(5463.0),
            ServicePlanNoticeMarginOffsetAdjuster(0.2),
            MinimumServicePlanNoticeAdjuster(3.1),
            TieredServicePlanNoticeAdjuster(
                mutableListOf<Double>(726.0, 2631.0, 4541.0),
                mutableListOf<Double>(1.2, 1.14, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 4095.0,
    )
}
