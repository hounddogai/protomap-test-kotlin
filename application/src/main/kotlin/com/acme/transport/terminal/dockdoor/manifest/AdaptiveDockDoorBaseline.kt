package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive dock door baseline is in its life cycle. */
enum class AdaptiveDockDoorBaselineStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive dock door baseline, with what the planners need to act on it. */
data class AdaptiveDockDoorBaseline(
    var code: String,
    var title: String,
    var depot: String,
    var attempts: Int,
    var rate: Double,
    var label: String,
    var status: AdaptiveDockDoorBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveDockDoorBaselineStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveDockDoorBaselineStatus): Boolean {
        when (status) {
            AdaptiveDockDoorBaselineStatus.PLANNED -> return next == AdaptiveDockDoorBaselineStatus.IN_PROGRESS || next == AdaptiveDockDoorBaselineStatus.COMPLETED
            AdaptiveDockDoorBaselineStatus.SCHEDULED -> return next == AdaptiveDockDoorBaselineStatus.COMPLETED
            AdaptiveDockDoorBaselineStatus.IN_PROGRESS -> {
                when (next) {
                    AdaptiveDockDoorBaselineStatus.COMPLETED, AdaptiveDockDoorBaselineStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveDockDoorBaselineStatus.COMPLETED -> {
                when (next) {
                    AdaptiveDockDoorBaselineStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveDockDoorBaselineStatus) {
        if (!canMoveTo(next)) {
            error("adaptive dock door baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive dock door baseline"} $code"

    fun projectedLoad(): Double = round((rate + attempts.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveDockDoorBaseline {
        return AdaptiveDockDoorBaseline(
            code = code,
            title = title,
            depot = depot,
            attempts = attempts,
            rate = rate,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive dock door baseline in status planned. */
fun newAdaptiveDockDoorBaseline(code: String, title: String): AdaptiveDockDoorBaseline {
    return AdaptiveDockDoorBaseline(
        code = code,
        title = title,
        depot = "",
        attempts = 0,
        rate = 0.0,
        label = "",
        status = AdaptiveDockDoorBaselineStatus.PLANNED,
    )
}
