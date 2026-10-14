package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching window is in its life cycle. */
enum class CoachingWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One coaching window, with what the planners need to act on it. */
data class CoachingWindow(
    var code: String,
    var label: String,
    var discount: Double,
    var tags: MutableList<String>,
    var threshold: Double,
    var vehicleId: String,
    var dock: String,
    var startsAt: Int,
    var status: CoachingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingWindowStatus): Boolean {
        when (status) {
            CoachingWindowStatus.DRAFT -> return next == CoachingWindowStatus.SUSPENDED
            CoachingWindowStatus.PENDING -> {
                when (next) {
                    CoachingWindowStatus.SUSPENDED, CoachingWindowStatus.CLOSED -> return true
                }
                return false
            }
            CoachingWindowStatus.ACTIVE -> return next == CoachingWindowStatus.CLOSED
            CoachingWindowStatus.SUSPENDED -> {
                when (next) {
                    CoachingWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingWindowStatus) {
        if (!canMoveTo(next)) {
            error("coaching window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"coaching window"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round((threshold + discount) * 100) / 100

    fun snapshot(): CoachingWindow {
        return CoachingWindow(
            code = code,
            label = label,
            discount = discount,
            tags = tags,
            threshold = threshold,
            vehicleId = vehicleId,
            dock = dock,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a coaching window in status draft. */
fun newCoachingWindow(code: String, label: String): CoachingWindow {
    return CoachingWindow(
        code = code,
        label = label,
        discount = 0.0,
        tags = mutableListOf<String>(),
        threshold = 0.0,
        vehicleId = "",
        dock = "",
        startsAt = 0,
        status = CoachingWindowStatus.DRAFT,
    )
}
