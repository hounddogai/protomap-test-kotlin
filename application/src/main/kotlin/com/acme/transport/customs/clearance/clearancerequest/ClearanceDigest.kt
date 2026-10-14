package com.acme.transport.customs.clearance.clearancerequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance digest is in its life cycle. */
enum class ClearanceDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a clearance digest and its current status. */
data class ClearanceDigest(
    var code: String,
    var title: String,
    var startsAt: Int,
    var minimum: Double,
    var externalRef: String,
    var status: ClearanceDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceDigestStatus): Boolean {
        when (status) {
            ClearanceDigestStatus.OPEN -> return next == ClearanceDigestStatus.RESOLVED
            ClearanceDigestStatus.IN_REVIEW -> return next == ClearanceDigestStatus.REOPENED
            ClearanceDigestStatus.RESOLVED -> {
                when (next) {
                    ClearanceDigestStatus.ARCHIVED -> return true
                }
                return false
            }
            ClearanceDigestStatus.REOPENED -> {
                when (next) {
                    ClearanceDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ClearanceDigestStatus) {
        if (!canMoveTo(next)) {
            error("clearance digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun projectedValue(): Double = round((startsAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): ClearanceDigest {
        return ClearanceDigest(
            code = code,
            title = title,
            startsAt = startsAt,
            minimum = minimum,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a clearance digest in status open. */
fun newClearanceDigest(code: String, title: String): ClearanceDigest {
    return ClearanceDigest(
        code = code,
        title = title,
        startsAt = 0,
        minimum = 0.0,
        externalRef = "",
        status = ClearanceDigestStatus.OPEN,
    )
}
