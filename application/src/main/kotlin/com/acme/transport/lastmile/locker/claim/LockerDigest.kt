package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker digest is in its life cycle. */
enum class LockerDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A locker digest. Its code is unique within a tenant. */
data class LockerDigest(
    var code: String,
    var title: String,
    var shiftCode: String,
    var carrier: String,
    var retries: Int,
    var externalRef: String,
    var lane: String,
    var budget: Double,
    var archived: Boolean,
    var status: LockerDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerDigestStatus): Boolean {
        when (status) {
            LockerDigestStatus.CREATED -> return next == LockerDigestStatus.VALIDATED
            LockerDigestStatus.VALIDATED -> return next == LockerDigestStatus.RETIRED
            LockerDigestStatus.PUBLISHED -> {
                when (next) {
                    LockerDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerDigestStatus) {
        if (!canMoveTo(next)) {
            error("locker digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"locker digest"} $code"

    fun effectiveExposure(): Double = round((retries.toDouble() - budget) * 100) / 100

    fun snapshot(): LockerDigest {
        return LockerDigest(
            code = code,
            title = title,
            shiftCode = shiftCode,
            carrier = carrier,
            retries = retries,
            externalRef = externalRef,
            lane = lane,
            budget = budget,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a locker digest in status created. */
fun newLockerDigest(code: String, title: String): LockerDigest {
    return LockerDigest(
        code = code,
        title = title,
        shiftCode = "",
        carrier = "",
        retries = 0,
        externalRef = "",
        lane = "",
        budget = 0.0,
        archived = false,
        status = LockerDigestStatus.CREATED,
    )
}
