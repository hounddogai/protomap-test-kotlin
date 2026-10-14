package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive backhaul target records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a adaptive backhaul target is in its life cycle. */
enum class AdaptiveBackhaulTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive backhaul target and its current status. */
data class AdaptiveBackhaulTarget(
    var code: String,
    var label: String,
    var version: Int,
    var ceiling: Double,
    var sequence: Int,
    var stopCount: Int,
    var revision: Int,
    var rate: Double,
    var locale: String,
    var status: AdaptiveBackhaulTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBackhaulTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBackhaulTargetStatus): Boolean {
        when (status) {
            AdaptiveBackhaulTargetStatus.PLANNED -> {
                when (next) {
                    AdaptiveBackhaulTargetStatus.IN_PROGRESS, AdaptiveBackhaulTargetStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveBackhaulTargetStatus.SCHEDULED -> return next == AdaptiveBackhaulTargetStatus.CANCELLED
            AdaptiveBackhaulTargetStatus.IN_PROGRESS -> return next == AdaptiveBackhaulTargetStatus.CANCELLED
            AdaptiveBackhaulTargetStatus.COMPLETED -> {
                when (next) {
                    AdaptiveBackhaulTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBackhaulTargetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive backhaul target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"adaptive backhaul target"} $code"

    fun effectiveLoad(): Double = round((stopCount.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveBackhaulTarget {
        return AdaptiveBackhaulTarget(
            code = code,
            label = label,
            version = version,
            ceiling = ceiling,
            sequence = sequence,
            stopCount = stopCount,
            revision = revision,
            rate = rate,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a adaptive backhaul target in status planned. */
fun newAdaptiveBackhaulTarget(code: String, label: String): AdaptiveBackhaulTarget {
    return AdaptiveBackhaulTarget(
        code = code,
        label = label,
        version = 0,
        ceiling = 0.0,
        sequence = 0,
        stopCount = 0,
        revision = 0,
        rate = 0.0,
        locale = "",
        status = AdaptiveBackhaulTargetStatus.PLANNED,
    )
}
