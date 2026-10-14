package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default trailer yard checkpoint is in its life cycle. */
enum class DefaultTrailerYardCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a default trailer yard checkpoint and its current status. */
data class DefaultTrailerYardCheckpoint(
    var code: String,
    var label: String,
    var dock: String,
    var loadKg: Double,
    var reviewer: String,
    var summary: String,
    var threshold: Double,
    var driverId: String,
    var locale: String,
    var stopCount: Int,
    var status: DefaultTrailerYardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTrailerYardCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTrailerYardCheckpointStatus): Boolean {
        when (status) {
            DefaultTrailerYardCheckpointStatus.OPEN -> return next == DefaultTrailerYardCheckpointStatus.REOPENED || next == DefaultTrailerYardCheckpointStatus.ARCHIVED
            DefaultTrailerYardCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    DefaultTrailerYardCheckpointStatus.RESOLVED, DefaultTrailerYardCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultTrailerYardCheckpointStatus.RESOLVED -> {
                when (next) {
                    DefaultTrailerYardCheckpointStatus.REOPENED, DefaultTrailerYardCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultTrailerYardCheckpointStatus.REOPENED -> {
                when (next) {
                    DefaultTrailerYardCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTrailerYardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("default trailer yard checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round((threshold - loadKg) * 100) / 100

    fun snapshot(): DefaultTrailerYardCheckpoint {
        return DefaultTrailerYardCheckpoint(
            code = code,
            label = label,
            dock = dock,
            loadKg = loadKg,
            reviewer = reviewer,
            summary = summary,
            threshold = threshold,
            driverId = driverId,
            locale = locale,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a default trailer yard checkpoint in status open. */
fun newDefaultTrailerYardCheckpoint(code: String, label: String): DefaultTrailerYardCheckpoint {
    return DefaultTrailerYardCheckpoint(
        code = code,
        label = label,
        dock = "",
        loadKg = 0.0,
        reviewer = "",
        summary = "",
        threshold = 0.0,
        driverId = "",
        locale = "",
        stopCount = 0,
        status = DefaultTrailerYardCheckpointStatus.OPEN,
    )
}
