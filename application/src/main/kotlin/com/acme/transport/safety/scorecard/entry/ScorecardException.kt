package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard exception is in its life cycle. */
enum class ScorecardExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scorecard exception. Its code is unique within a tenant. */
data class ScorecardException(
    var code: String,
    var title: String,
    var trailerId: String,
    var locale: String,
    var dock: String,
    var limit: Int,
    var graceDays: Int,
    var status: ScorecardExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardExceptionStatus): Boolean {
        when (status) {
            ScorecardExceptionStatus.PLANNED -> {
                when (next) {
                    ScorecardExceptionStatus.IN_PROGRESS, ScorecardExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            ScorecardExceptionStatus.SCHEDULED -> return next == ScorecardExceptionStatus.IN_PROGRESS || next == ScorecardExceptionStatus.CANCELLED
            ScorecardExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    ScorecardExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            ScorecardExceptionStatus.COMPLETED -> {
                when (next) {
                    ScorecardExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardExceptionStatus) {
        if (!canMoveTo(next)) {
            error("scorecard exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round((graceDays.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): ScorecardException {
        return ScorecardException(
            code = code,
            title = title,
            trailerId = trailerId,
            locale = locale,
            dock = dock,
            limit = limit,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scorecard exception in status planned. */
fun newScorecardException(code: String, title: String): ScorecardException {
    return ScorecardException(
        code = code,
        title = title,
        trailerId = "",
        locale = "",
        dock = "",
        limit = 0,
        graceDays = 0,
        status = ScorecardExceptionStatus.PLANNED,
    )
}
