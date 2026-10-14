package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization segment is in its life cycle. */
enum class UtilizationSegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One utilization segment, with what the planners need to act on it. */
data class UtilizationSegment(
    var code: String,
    var label: String,
    var attempts: Int,
    var graceDays: Int,
    var createdAt: Int,
    var locale: String,
    var status: UtilizationSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationSegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationSegmentStatus): Boolean {
        when (status) {
            UtilizationSegmentStatus.NEW -> return next == UtilizationSegmentStatus.QUEUED || next == UtilizationSegmentStatus.FAILED
            UtilizationSegmentStatus.QUEUED -> return next == UtilizationSegmentStatus.FAILED
            UtilizationSegmentStatus.RUNNING -> return next == UtilizationSegmentStatus.DONE || next == UtilizationSegmentStatus.FAILED
            UtilizationSegmentStatus.DONE -> {
                when (next) {
                    UtilizationSegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationSegmentStatus) {
        if (!canMoveTo(next)) {
            error("utilization segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"utilization segment"} $code"

    fun netExposure(): Double = round(createdAt.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): UtilizationSegment {
        return UtilizationSegment(
            code = code,
            label = label,
            attempts = attempts,
            graceDays = graceDays,
            createdAt = createdAt,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a utilization segment in status new. */
fun newUtilizationSegment(code: String, label: String): UtilizationSegment {
    return UtilizationSegment(
        code = code,
        label = label,
        attempts = 0,
        graceDays = 0,
        createdAt = 0,
        locale = "",
        status = UtilizationSegmentStatus.NEW,
    )
}
