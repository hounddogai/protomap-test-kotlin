package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback lane plan notice calculation. */
interface FallbackLanePlanNoticeAdjuster {
    fun describe(): String
    fun adjust(item: FallbackLanePlanNotice, value: Double): Double
}

data class PercentFallbackLanePlanNoticeAdjuster(var percent: Double) : FallbackLanePlanNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackLanePlanNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedFallbackLanePlanNoticeAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackLanePlanNoticeAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackLanePlanNotice, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotFallbackLanePlanNoticeAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackLanePlanNoticeAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackLanePlanNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback lane plan notice steps in order and keeps a trace of each. */
data class FallbackLanePlanNoticeEngine(
    var steps: MutableList<FallbackLanePlanNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FallbackLanePlanNotice): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackLanePlanNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFallbackLanePlanNoticeEngine(flags: Registry): FallbackLanePlanNoticeEngine {
    return FallbackLanePlanNoticeEngine(
        steps = mutableListOf<FallbackLanePlanNoticeAdjuster>(
            PercentFallbackLanePlanNoticeAdjuster(2.1),
            FlaggedFallbackLanePlanNoticeAdjuster(flags, "fallback-lane-plan-notice-manual", 0.3),
            DepotFallbackLanePlanNoticeAdjuster(
                mutableMapOf<String, Double>("central" to 1.26, "online" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 30870.7,
    )
}
