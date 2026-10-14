package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.text.normalize

/** Where a cutoff score is in its life cycle. */
enum class CutoffScoreStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One cutoff score, with what the planners need to act on it. */
data class CutoffScore(
    var code: String,
    var title: String,
    var name: String,
    var active: Boolean,
    var contactEmail: String,
    var distanceKm: Double,
    var currency: String,
    var status: CutoffScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffScoreStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffScoreStatus): Boolean {
        when (status) {
            CutoffScoreStatus.PROPOSED -> {
                when (next) {
                    CutoffScoreStatus.WITHDRAWN -> return true
                }
                return false
            }
            CutoffScoreStatus.APPROVED -> return next == CutoffScoreStatus.REJECTED
            CutoffScoreStatus.REJECTED -> return next == CutoffScoreStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CutoffScoreStatus) {
        if (!canMoveTo(next)) {
            error("cutoff score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"cutoff score"} $code"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): CutoffScore {
        return CutoffScore(
            code = code,
            title = title,
            name = name,
            active = active,
            contactEmail = contactEmail,
            distanceKm = distanceKm,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cutoff score in status proposed. */
fun newCutoffScore(code: String, title: String): CutoffScore {
    return CutoffScore(
        code = code,
        title = title,
        name = "",
        active = false,
        contactEmail = "",
        distanceKm = 0.0,
        currency = "",
        status = CutoffScoreStatus.PROPOSED,
    )
}
