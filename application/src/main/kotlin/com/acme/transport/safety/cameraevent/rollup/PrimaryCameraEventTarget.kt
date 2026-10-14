package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary camera event target is in its life cycle. */
enum class PrimaryCameraEventTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary camera event target, with what the planners need to act on it. */
data class PrimaryCameraEventTarget(
    var code: String,
    var title: String,
    var floorValue: Double,
    var category: String,
    var margin: Double,
    var status: PrimaryCameraEventTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCameraEventTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCameraEventTargetStatus): Boolean {
        when (status) {
            PrimaryCameraEventTargetStatus.OPEN -> {
                when (next) {
                    PrimaryCameraEventTargetStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryCameraEventTargetStatus.IN_REVIEW -> {
                when (next) {
                    PrimaryCameraEventTargetStatus.RESOLVED -> return true
                }
                return false
            }
            PrimaryCameraEventTargetStatus.RESOLVED -> return next == PrimaryCameraEventTargetStatus.REOPENED || next == PrimaryCameraEventTargetStatus.ARCHIVED
            PrimaryCameraEventTargetStatus.REOPENED -> {
                when (next) {
                    PrimaryCameraEventTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCameraEventTargetStatus) {
        if (!canMoveTo(next)) {
            error("primary camera event target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round((margin - floorValue) * 100) / 100

    fun snapshot(): PrimaryCameraEventTarget {
        return PrimaryCameraEventTarget(
            code = code,
            title = title,
            floorValue = floorValue,
            category = category,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary camera event target in status open. */
fun newPrimaryCameraEventTarget(code: String, title: String): PrimaryCameraEventTarget {
    return PrimaryCameraEventTarget(
        code = code,
        title = title,
        floorValue = 0.0,
        category = "",
        margin = 0.0,
        status = PrimaryCameraEventTargetStatus.OPEN,
    )
}
