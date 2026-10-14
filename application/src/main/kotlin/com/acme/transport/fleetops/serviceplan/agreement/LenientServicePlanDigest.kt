package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient service plan digest is in its life cycle. */
enum class LenientServicePlanDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient service plan digest, with what the planners need to act on it. */
data class LenientServicePlanDigest(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var createdAt: Int,
    var attempts: Int,
    var expiresAt: Int,
    var checksum: String,
    var approved: Boolean,
    var startsAt: Int,
    var vehicleId: String,
    var status: LenientServicePlanDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientServicePlanDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientServicePlanDigestStatus): Boolean {
        when (status) {
            LenientServicePlanDigestStatus.CREATED -> return next == LenientServicePlanDigestStatus.VALIDATED || next == LenientServicePlanDigestStatus.PUBLISHED
            LenientServicePlanDigestStatus.VALIDATED -> return next == LenientServicePlanDigestStatus.PUBLISHED
            LenientServicePlanDigestStatus.PUBLISHED -> {
                when (next) {
                    LenientServicePlanDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientServicePlanDigestStatus) {
        if (!canMoveTo(next)) {
            error("lenient service plan digest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((createdAt.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): LenientServicePlanDigest {
        return LenientServicePlanDigest(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            createdAt = createdAt,
            attempts = attempts,
            expiresAt = expiresAt,
            checksum = checksum,
            approved = approved,
            startsAt = startsAt,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient service plan digest in status created. */
fun newLenientServicePlanDigest(code: String, title: String): LenientServicePlanDigest {
    return LenientServicePlanDigest(
        code = code,
        title = title,
        durationMinutes = 0,
        createdAt = 0,
        attempts = 0,
        expiresAt = 0,
        checksum = "",
        approved = false,
        startsAt = 0,
        vehicleId = "",
        status = LenientServicePlanDigestStatus.CREATED,
    )
}
