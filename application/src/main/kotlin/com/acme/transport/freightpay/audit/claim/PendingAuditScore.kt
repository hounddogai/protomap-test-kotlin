package com.acme.transport.freightpay.audit.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending audit score is in its life cycle. */
enum class PendingAuditScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending audit score, with what the planners need to act on it. */
data class PendingAuditScore(
    var code: String,
    var title: String,
    var graceDays: Int,
    var createdAt: Int,
    var phone: String,
    var status: PendingAuditScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAuditScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAuditScoreStatus): Boolean {
        when (status) {
            PendingAuditScoreStatus.NEW -> {
                when (next) {
                    PendingAuditScoreStatus.DONE -> return true
                }
                return false
            }
            PendingAuditScoreStatus.QUEUED -> {
                when (next) {
                    PendingAuditScoreStatus.RUNNING, PendingAuditScoreStatus.FAILED -> return true
                }
                return false
            }
            PendingAuditScoreStatus.RUNNING -> return next == PendingAuditScoreStatus.DONE
            PendingAuditScoreStatus.DONE -> {
                when (next) {
                    PendingAuditScoreStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAuditScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending audit score $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((createdAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): PendingAuditScore {
        return PendingAuditScore(
            code = code,
            title = title,
            graceDays = graceDays,
            createdAt = createdAt,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending audit score in status new. */
fun newPendingAuditScore(code: String, title: String): PendingAuditScore {
    return PendingAuditScore(
        code = code,
        title = title,
        graceDays = 0,
        createdAt = 0,
        phone = "",
        status = PendingAuditScoreStatus.NEW,
    )
}
