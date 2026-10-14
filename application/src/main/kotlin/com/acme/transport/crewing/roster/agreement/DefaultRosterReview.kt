package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default roster review is in its life cycle. */
enum class DefaultRosterReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default roster review. Its code is unique within a tenant. */
data class DefaultRosterReview(
    var code: String,
    var title: String,
    var ceiling: Double,
    var notes: String,
    var shiftCode: String,
    var rate: Double,
    var graceDays: Int,
    var updatedAt: Int,
    var externalRef: String,
    var stopCount: Int,
    var status: DefaultRosterReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRosterReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRosterReviewStatus): Boolean {
        when (status) {
            DefaultRosterReviewStatus.PLANNED -> return next == DefaultRosterReviewStatus.IN_PROGRESS || next == DefaultRosterReviewStatus.COMPLETED
            DefaultRosterReviewStatus.SCHEDULED -> {
                when (next) {
                    DefaultRosterReviewStatus.IN_PROGRESS, DefaultRosterReviewStatus.COMPLETED -> return true
                }
                return false
            }
            DefaultRosterReviewStatus.IN_PROGRESS -> {
                when (next) {
                    DefaultRosterReviewStatus.COMPLETED, DefaultRosterReviewStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultRosterReviewStatus.COMPLETED -> return next == DefaultRosterReviewStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultRosterReviewStatus) {
        if (!canMoveTo(next)) {
            error("default roster review $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round((graceDays.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultRosterReview {
        return DefaultRosterReview(
            code = code,
            title = title,
            ceiling = ceiling,
            notes = notes,
            shiftCode = shiftCode,
            rate = rate,
            graceDays = graceDays,
            updatedAt = updatedAt,
            externalRef = externalRef,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default roster review in status planned. */
fun newDefaultRosterReview(code: String, title: String): DefaultRosterReview {
    return DefaultRosterReview(
        code = code,
        title = title,
        ceiling = 0.0,
        notes = "",
        shiftCode = "",
        rate = 0.0,
        graceDays = 0,
        updatedAt = 0,
        externalRef = "",
        stopCount = 0,
        status = DefaultRosterReviewStatus.PLANNED,
    )
}
