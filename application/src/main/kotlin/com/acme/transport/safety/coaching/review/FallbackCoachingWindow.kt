package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback coaching window is in its life cycle. */
enum class FallbackCoachingWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One fallback coaching window, with what the planners need to act on it. */
data class FallbackCoachingWindow(
    var code: String,
    var title: String,
    var ceiling: Double,
    var createdAt: Int,
    var capacity: Int,
    var locale: String,
    var segment: String,
    var loadKg: Double,
    var checksum: String,
    var score: Double,
    var status: FallbackCoachingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCoachingWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCoachingWindowStatus): Boolean {
        when (status) {
            FallbackCoachingWindowStatus.PROPOSED -> return next == FallbackCoachingWindowStatus.APPROVED || next == FallbackCoachingWindowStatus.REJECTED
            FallbackCoachingWindowStatus.APPROVED -> return next == FallbackCoachingWindowStatus.REJECTED || next == FallbackCoachingWindowStatus.WITHDRAWN
            FallbackCoachingWindowStatus.REJECTED -> {
                when (next) {
                    FallbackCoachingWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCoachingWindowStatus) {
        if (!canMoveTo(next)) {
            error("fallback coaching window $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((ceiling + createdAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackCoachingWindow {
        return FallbackCoachingWindow(
            code = code,
            title = title,
            ceiling = ceiling,
            createdAt = createdAt,
            capacity = capacity,
            locale = locale,
            segment = segment,
            loadKg = loadKg,
            checksum = checksum,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback coaching window in status proposed. */
fun newFallbackCoachingWindow(code: String, title: String): FallbackCoachingWindow {
    return FallbackCoachingWindow(
        code = code,
        title = title,
        ceiling = 0.0,
        createdAt = 0,
        capacity = 0,
        locale = "",
        segment = "",
        loadKg = 0.0,
        checksum = "",
        score = 0.0,
        status = FallbackCoachingWindowStatus.PROPOSED,
    )
}
