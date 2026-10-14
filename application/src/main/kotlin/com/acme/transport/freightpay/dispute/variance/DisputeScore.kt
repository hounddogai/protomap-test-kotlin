package com.acme.transport.freightpay.dispute.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute score is in its life cycle. */
enum class DisputeScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dispute score as the dispute team keeps it. */
data class DisputeScore(
    var code: String,
    var title: String,
    var timezone: String,
    var notes: String,
    var endsAt: Int,
    var createdAt: Int,
    var unit: String,
    var quantity: Int,
    var status: DisputeScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeScoreStatus): Boolean {
        when (status) {
            DisputeScoreStatus.OPEN -> return next == DisputeScoreStatus.ARCHIVED
            DisputeScoreStatus.IN_REVIEW -> return next == DisputeScoreStatus.RESOLVED
            DisputeScoreStatus.RESOLVED -> return next == DisputeScoreStatus.ARCHIVED
            DisputeScoreStatus.REOPENED -> {
                when (next) {
                    DisputeScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeScoreStatus) {
        if (!canMoveTo(next)) {
            error("dispute score $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dispute score"} $code"

    fun weightedTotal(): Double = round((endsAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): DisputeScore {
        return DisputeScore(
            code = code,
            title = title,
            timezone = timezone,
            notes = notes,
            endsAt = endsAt,
            createdAt = createdAt,
            unit = unit,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a dispute score in status open. */
fun newDisputeScore(code: String, title: String): DisputeScore {
    return DisputeScore(
        code = code,
        title = title,
        timezone = "",
        notes = "",
        endsAt = 0,
        createdAt = 0,
        unit = "",
        quantity = 0,
        status = DisputeScoreStatus.OPEN,
    )
}
