package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender score is in its life cycle. */
enum class TenderScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One tender score, with what the planners need to act on it. */
data class TenderScore(
    var code: String,
    var label: String,
    var retries: Int,
    var startsAt: Int,
    var priority: Int,
    var archived: Boolean,
    var title: String,
    var summary: String,
    var depot: String,
    var status: TenderScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderScoreStatus): Boolean {
        when (status) {
            TenderScoreStatus.DRAFT -> return next == TenderScoreStatus.SUSPENDED || next == TenderScoreStatus.CLOSED
            TenderScoreStatus.PENDING -> return next == TenderScoreStatus.CLOSED
            TenderScoreStatus.ACTIVE -> return next == TenderScoreStatus.SUSPENDED || next == TenderScoreStatus.CLOSED
            TenderScoreStatus.SUSPENDED -> return next == TenderScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TenderScoreStatus) {
        if (!canMoveTo(next)) {
            error("tender score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tender score"} $code"

    fun adjustedVolume(): Double = round(priority.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): TenderScore {
        return TenderScore(
            code = code,
            label = label,
            retries = retries,
            startsAt = startsAt,
            priority = priority,
            archived = archived,
            title = title,
            summary = summary,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tender score in status draft. */
fun newTenderScore(code: String, label: String): TenderScore {
    return TenderScore(
        code = code,
        label = label,
        retries = 0,
        startsAt = 0,
        priority = 0,
        archived = false,
        title = "",
        summary = "",
        depot = "",
        status = TenderScoreStatus.DRAFT,
    )
}
