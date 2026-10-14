package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped parcel digest is in its life cycle. */
enum class CappedParcelDigestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped parcel digest. Its code is unique within a tenant. */
data class CappedParcelDigest(
    var code: String,
    var title: String,
    var rate: Double,
    var fuelLiters: Double,
    var score: Double,
    var status: CappedParcelDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedParcelDigestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedParcelDigestStatus): Boolean {
        when (status) {
            CappedParcelDigestStatus.OPEN -> return next == CappedParcelDigestStatus.REOPENED
            CappedParcelDigestStatus.IN_REVIEW -> return next == CappedParcelDigestStatus.ARCHIVED
            CappedParcelDigestStatus.RESOLVED -> return next == CappedParcelDigestStatus.ARCHIVED
            CappedParcelDigestStatus.REOPENED -> {
                when (next) {
                    CappedParcelDigestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedParcelDigestStatus) {
        if (!canMoveTo(next)) {
            error("capped parcel digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((score + rate) * 100) / 100

    fun snapshot(): CappedParcelDigest {
        return CappedParcelDigest(
            code = code,
            title = title,
            rate = rate,
            fuelLiters = fuelLiters,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped parcel digest in status open. */
fun newCappedParcelDigest(code: String, title: String): CappedParcelDigest {
    return CappedParcelDigest(
        code = code,
        title = title,
        rate = 0.0,
        fuelLiters = 0.0,
        score = 0.0,
        status = CappedParcelDigestStatus.OPEN,
    )
}
