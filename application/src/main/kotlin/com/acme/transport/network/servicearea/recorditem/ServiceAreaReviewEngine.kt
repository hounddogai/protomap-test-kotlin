package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the service area review calculation. */
interface ServiceAreaReviewModifier {
    fun describe(): String
    fun adjust(item: ServiceAreaReview, value: Double): Double
}

data class TieredServiceAreaReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ServiceAreaReviewModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ServiceAreaReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ServiceAreaReviewBatchSizeOffsetAdjuster(var weight: Double) : ServiceAreaReviewModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: ServiceAreaReview, value: Double): Double = value + item.batchSize.toDouble() * weight
}

data class FlaggedServiceAreaReviewAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ServiceAreaReviewModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ServiceAreaReview, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the service area review steps in order and keeps a trace of each. */
data class ServiceAreaReviewEngine(
    var steps: MutableList<ServiceAreaReviewModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ServiceAreaReview): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ServiceAreaReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultServiceAreaReviewEngine(flags: Registry): ServiceAreaReviewEngine {
    return ServiceAreaReviewEngine(
        steps = mutableListOf<ServiceAreaReviewModifier>(
            TieredServiceAreaReviewAdjuster(
                mutableListOf<Double>(664.0, 701.0, 6369.0, 7521.0),
                mutableListOf<Double>(1.02, 1.1, 0.97, 1.01),
            ),
            ServiceAreaReviewBatchSizeOffsetAdjuster(1.63),
            FlaggedServiceAreaReviewAdjuster(flags, "service-area-review-seasonal", 8.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 59483.5,
    )
}
