package com.acme.transport.safety.incident.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback incident window is in its life cycle. */
enum class FallbackIncidentWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback incident window, with what the planners need to act on it. */
data class FallbackIncidentWindow(
    var code: String,
    var label: String,
    var batchSize: Int,
    var threshold: Double,
    var title: String,
    var trailerId: String,
    var windowDays: Int,
    var status: FallbackIncidentWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackIncidentWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackIncidentWindowStatus): Boolean {
        when (status) {
            FallbackIncidentWindowStatus.OPEN -> {
                when (next) {
                    FallbackIncidentWindowStatus.IN_REVIEW, FallbackIncidentWindowStatus.RESOLVED -> return true
                }
                return false
            }
            FallbackIncidentWindowStatus.IN_REVIEW -> return next == FallbackIncidentWindowStatus.RESOLVED
            FallbackIncidentWindowStatus.RESOLVED -> return next == FallbackIncidentWindowStatus.REOPENED || next == FallbackIncidentWindowStatus.ARCHIVED
            FallbackIncidentWindowStatus.REOPENED -> return next == FallbackIncidentWindowStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackIncidentWindowStatus) {
        if (!canMoveTo(next)) {
            error("fallback incident window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"fallback incident window"} $code"

    fun weightedValue(): Double = round((threshold - windowDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackIncidentWindow {
        return FallbackIncidentWindow(
            code = code,
            label = label,
            batchSize = batchSize,
            threshold = threshold,
            title = title,
            trailerId = trailerId,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback incident window in status open. */
fun newFallbackIncidentWindow(code: String, label: String): FallbackIncidentWindow {
    return FallbackIncidentWindow(
        code = code,
        label = label,
        batchSize = 0,
        threshold = 0.0,
        title = "",
        trailerId = "",
        windowDays = 0,
        status = FallbackIncidentWindowStatus.OPEN,
    )
}
