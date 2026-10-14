package com.acme.transport.safety.coaching.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching notice records of safety's coaching work, and the rules, stores, and calculations around them.

/** Where a coaching notice is in its life cycle. */
enum class CoachingNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One coaching notice, with what the planners need to act on it. */
data class CoachingNotice(
    var code: String,
    var name: String,
    var budget: Double,
    var currency: String,
    var category: String,
    var createdAt: Int,
    var dock: String,
    var checksum: String,
    var graceDays: Int,
    var status: CoachingNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingNoticeStatus): Boolean {
        when (status) {
            CoachingNoticeStatus.OPEN -> return next == CoachingNoticeStatus.ARCHIVED
            CoachingNoticeStatus.IN_REVIEW -> {
                when (next) {
                    CoachingNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            CoachingNoticeStatus.RESOLVED -> return next == CoachingNoticeStatus.REOPENED
            CoachingNoticeStatus.REOPENED -> {
                when (next) {
                    CoachingNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingNoticeStatus) {
        if (!canMoveTo(next)) {
            error("coaching notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round((graceDays.toDouble() + budget) * 100) / 100

    fun snapshot(): CoachingNotice {
        return CoachingNotice(
            code = code,
            name = name,
            budget = budget,
            currency = currency,
            category = category,
            createdAt = createdAt,
            dock = dock,
            checksum = checksum,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a coaching notice in status open. */
fun newCoachingNotice(code: String, name: String): CoachingNotice {
    return CoachingNotice(
        code = code,
        name = name,
        budget = 0.0,
        currency = "",
        category = "",
        createdAt = 0,
        dock = "",
        checksum = "",
        graceDays = 0,
        status = CoachingNoticeStatus.OPEN,
    )
}
