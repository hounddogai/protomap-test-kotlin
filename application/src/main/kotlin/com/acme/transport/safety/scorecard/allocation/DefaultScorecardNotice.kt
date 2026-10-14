package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default scorecard notice is in its life cycle. */
enum class DefaultScorecardNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a default scorecard notice and its current status. */
data class DefaultScorecardNotice(
    var code: String,
    var title: String,
    var archived: Boolean,
    var loadKg: Double,
    var version: Int,
    var createdAt: Int,
    var contactEmail: String,
    var depot: String,
    var graceDays: Int,
    var status: DefaultScorecardNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultScorecardNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultScorecardNoticeStatus): Boolean {
        when (status) {
            DefaultScorecardNoticeStatus.PROPOSED -> {
                when (next) {
                    DefaultScorecardNoticeStatus.APPROVED, DefaultScorecardNoticeStatus.REJECTED -> return true
                }
                return false
            }
            DefaultScorecardNoticeStatus.APPROVED -> return next == DefaultScorecardNoticeStatus.REJECTED || next == DefaultScorecardNoticeStatus.WITHDRAWN
            DefaultScorecardNoticeStatus.REJECTED -> {
                when (next) {
                    DefaultScorecardNoticeStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultScorecardNoticeStatus) {
        if (!canMoveTo(next)) {
            error("default scorecard notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"default scorecard notice"} $code"

    fun weightedVolume(): Double = round((loadKg - createdAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultScorecardNotice {
        return DefaultScorecardNotice(
            code = code,
            title = title,
            archived = archived,
            loadKg = loadKg,
            version = version,
            createdAt = createdAt,
            contactEmail = contactEmail,
            depot = depot,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default scorecard notice in status proposed. */
fun newDefaultScorecardNotice(code: String, title: String): DefaultScorecardNotice {
    return DefaultScorecardNotice(
        code = code,
        title = title,
        archived = false,
        loadKg = 0.0,
        version = 0,
        createdAt = 0,
        contactEmail = "",
        depot = "",
        graceDays = 0,
        status = DefaultScorecardNoticeStatus.PROPOSED,
    )
}
