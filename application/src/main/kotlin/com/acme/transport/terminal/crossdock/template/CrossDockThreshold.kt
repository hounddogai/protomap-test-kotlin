package com.acme.transport.terminal.crossdock.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock threshold is in its life cycle. */
enum class CrossDockThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One cross dock threshold, with what the planners need to act on it. */
data class CrossDockThreshold(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var capacity: Int,
    var dock: String,
    var vehicleId: String,
    var archived: Boolean,
    var notes: String,
    var weight: Double,
    var summary: String,
    var attempts: Int,
    var status: CrossDockThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockThresholdStatus): Boolean {
        when (status) {
            CrossDockThresholdStatus.OPEN -> {
                when (next) {
                    CrossDockThresholdStatus.REOPENED -> return true
                }
                return false
            }
            CrossDockThresholdStatus.IN_REVIEW -> return next == CrossDockThresholdStatus.RESOLVED
            CrossDockThresholdStatus.RESOLVED -> return next == CrossDockThresholdStatus.REOPENED || next == CrossDockThresholdStatus.ARCHIVED
            CrossDockThresholdStatus.REOPENED -> return next == CrossDockThresholdStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CrossDockThresholdStatus) {
        if (!canMoveTo(next)) {
            error("cross dock threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"cross dock threshold"} $code"

    fun adjustedValue(): Double = round((capacity.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CrossDockThreshold {
        return CrossDockThreshold(
            code = code,
            label = label,
            expiresAt = expiresAt,
            capacity = capacity,
            dock = dock,
            vehicleId = vehicleId,
            archived = archived,
            notes = notes,
            weight = weight,
            summary = summary,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cross dock threshold in status open. */
fun newCrossDockThreshold(code: String, label: String): CrossDockThreshold {
    return CrossDockThreshold(
        code = code,
        label = label,
        expiresAt = 0,
        capacity = 0,
        dock = "",
        vehicleId = "",
        archived = false,
        notes = "",
        weight = 0.0,
        summary = "",
        attempts = 0,
        status = CrossDockThresholdStatus.OPEN,
    )
}
