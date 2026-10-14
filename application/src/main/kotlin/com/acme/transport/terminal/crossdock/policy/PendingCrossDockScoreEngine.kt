package com.acme.transport.terminal.crossdock.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending cross dock score calculation. */
interface PendingCrossDockScoreStep {
    fun describe(): String
    fun adjust(item: PendingCrossDockScore, value: Double): Double
}

data class PendingCrossDockScoreRevisionOffsetAdjuster(var weight: Double) : PendingCrossDockScoreStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: PendingCrossDockScore, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class PercentPendingCrossDockScoreAdjuster(var percent: Double) : PendingCrossDockScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCrossDockScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingCrossDockScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCrossDockScoreStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCrossDockScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingCrossDockScoreAdjuster(var step: Double) : PendingCrossDockScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCrossDockScore, value: Double): Double = roundTo(value, step)
}

data class TitlePendingCrossDockScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCrossDockScoreStep {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingCrossDockScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending cross dock score steps in order and keeps a trace of each. */
data class PendingCrossDockScoreEngine(
    var steps: MutableList<PendingCrossDockScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingCrossDockScore): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingCrossDockScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingCrossDockScoreEngine(flags: Registry): PendingCrossDockScoreEngine {
    return PendingCrossDockScoreEngine(
        steps = mutableListOf<PendingCrossDockScoreStep>(
            PendingCrossDockScoreRevisionOffsetAdjuster(1.73),
            PercentPendingCrossDockScoreAdjuster(-16.1),
            FlaggedPendingCrossDockScoreAdjuster(flags, "pending-cross-dock-score-tiered", -6.1),
            RoundingPendingCrossDockScoreAdjuster(5.0),
            TitlePendingCrossDockScoreAdjuster(
                mutableMapOf<String, Double>("central" to 0.75, "retail" to 1.08, "east" to 1.3),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 13493.4,
    )
}
