package com.acme.transport.network.hub.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy hub record calculation. */
interface LegacyHubRecordModifier {
    fun describe(): String
    fun adjust(item: LegacyHubRecord, value: Double): Double
}

data class RoundingLegacyHubRecordAdjuster(var step: Double) : LegacyHubRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyHubRecord, value: Double): Double = roundTo(value, step)
}

data class CapLegacyHubRecordAdjuster(var cap: Double) : LegacyHubRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyHubRecord, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyHubRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyHubRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyHubRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy hub record steps in order and keeps a trace of each. */
data class LegacyHubRecordEngine(
    var steps: MutableList<LegacyHubRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyHubRecord): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyHubRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyHubRecordEngine(flags: Registry): LegacyHubRecordEngine {
    return LegacyHubRecordEngine(
        steps = mutableListOf<LegacyHubRecordModifier>(
            RoundingLegacyHubRecordAdjuster(0.05),
            CapLegacyHubRecordAdjuster(31483.0),
            FlaggedLegacyHubRecordAdjuster(flags, "legacy-hub-record-regional", -6.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 63811.0,
    )
}
