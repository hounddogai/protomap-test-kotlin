package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.text.normalize

/** Where a capped coaching digest is in its life cycle. */
enum class CappedCoachingDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped coaching digest and its current status. */
data class CappedCoachingDigest(
    var code: String,
    var name: String,
    var phone: String,
    var archived: Boolean,
    var summary: String,
    var amount: Double,
    var status: CappedCoachingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCoachingDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCoachingDigestStatus): Boolean {
        when (status) {
            CappedCoachingDigestStatus.REQUESTED -> {
                when (next) {
                    CappedCoachingDigestStatus.REVOKED -> return true
                }
                return false
            }
            CappedCoachingDigestStatus.GRANTED -> {
                when (next) {
                    CappedCoachingDigestStatus.EXPIRED -> return true
                }
                return false
            }
            CappedCoachingDigestStatus.REVOKED -> return next == CappedCoachingDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedCoachingDigestStatus) {
        if (!canMoveTo(next)) {
            error("capped coaching digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = amount

    fun snapshot(): CappedCoachingDigest {
        return CappedCoachingDigest(
            code = code,
            name = name,
            phone = phone,
            archived = archived,
            summary = summary,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped coaching digest in status requested. */
fun newCappedCoachingDigest(code: String, name: String): CappedCoachingDigest {
    return CappedCoachingDigest(
        code = code,
        name = name,
        phone = "",
        archived = false,
        summary = "",
        amount = 0.0,
        status = CappedCoachingDigestStatus.REQUESTED,
    )
}
