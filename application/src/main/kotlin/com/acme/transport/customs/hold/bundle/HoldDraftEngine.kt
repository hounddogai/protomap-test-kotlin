package com.acme.transport.customs.hold.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hold draft calculation. */
interface HoldDraftModifier {
    fun describe(): String
    fun adjust(item: HoldDraft, value: Double): Double
}

data class RoundingHoldDraftAdjuster(var step: Double) : HoldDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldDraft, value: Double): Double = roundTo(value, step)
}

data class HoldDraftPriorityOffsetAdjuster(var weight: Double) : HoldDraftModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: HoldDraft, value: Double): Double = value + item.priority.toDouble() * weight
}

data class FlaggedHoldDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HoldDraftModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HoldDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the hold draft steps in order and keeps a trace of each. */
data class HoldDraftEngine(
    var steps: MutableList<HoldDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: HoldDraft): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultHoldDraftEngine(flags: Registry): HoldDraftEngine {
    return HoldDraftEngine(
        steps = mutableListOf<HoldDraftModifier>(
            RoundingHoldDraftAdjuster(0.5),
            HoldDraftPriorityOffsetAdjuster(1.96),
            FlaggedHoldDraftAdjuster(flags, "hold-draft-manual", 5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 39349.5,
    )
}
