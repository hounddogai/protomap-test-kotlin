package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending tender score is in its life cycle. */
enum class PendingTenderScoreStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One pending tender score, with what the planners need to act on it. */
data class PendingTenderScore(
    var code: String,
    var name: String,
    var active: Boolean,
    var checksum: String,
    var weight: Double,
    var approved: Boolean,
    var ratio: Double,
    var ownerId: String,
    var status: PendingTenderScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTenderScoreStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTenderScoreStatus): Boolean {
        when (status) {
            PendingTenderScoreStatus.NEW -> {
                when (next) {
                    PendingTenderScoreStatus.QUEUED -> return true
                }
                return false
            }
            PendingTenderScoreStatus.QUEUED -> {
                when (next) {
                    PendingTenderScoreStatus.RUNNING -> return true
                }
                return false
            }
            PendingTenderScoreStatus.RUNNING -> {
                when (next) {
                    PendingTenderScoreStatus.DONE, PendingTenderScoreStatus.FAILED -> return true
                }
                return false
            }
            PendingTenderScoreStatus.DONE -> return next == PendingTenderScoreStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PendingTenderScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending tender score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((weight - ratio) * 100) / 100

    fun snapshot(): PendingTenderScore {
        return PendingTenderScore(
            code = code,
            name = name,
            active = active,
            checksum = checksum,
            weight = weight,
            approved = approved,
            ratio = ratio,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending tender score in status new. */
fun newPendingTenderScore(code: String, name: String): PendingTenderScore {
    return PendingTenderScore(
        code = code,
        name = name,
        active = false,
        checksum = "",
        weight = 0.0,
        approved = false,
        ratio = 0.0,
        ownerId = "",
        status = PendingTenderScoreStatus.NEW,
    )
}
