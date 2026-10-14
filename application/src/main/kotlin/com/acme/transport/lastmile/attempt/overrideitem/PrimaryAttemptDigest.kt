package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary attempt digest is in its life cycle. */
enum class PrimaryAttemptDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary attempt digest and its current status. */
data class PrimaryAttemptDigest(
    var code: String,
    var label: String,
    var visible: Boolean,
    var threshold: Double,
    var approved: Boolean,
    var fuelLiters: Double,
    var expiresAt: Int,
    var name: String,
    var cost: Double,
    var dock: String,
    var durationMinutes: Int,
    var status: PrimaryAttemptDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAttemptDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAttemptDigestStatus): Boolean {
        when (status) {
            PrimaryAttemptDigestStatus.REQUESTED -> return next == PrimaryAttemptDigestStatus.REVOKED || next == PrimaryAttemptDigestStatus.EXPIRED
            PrimaryAttemptDigestStatus.GRANTED -> {
                when (next) {
                    PrimaryAttemptDigestStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryAttemptDigestStatus.REVOKED -> return next == PrimaryAttemptDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryAttemptDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary attempt digest $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary attempt digest"} $code"

    fun weightedLoad(): Double = round((expiresAt.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): PrimaryAttemptDigest {
        return PrimaryAttemptDigest(
            code = code,
            label = label,
            visible = visible,
            threshold = threshold,
            approved = approved,
            fuelLiters = fuelLiters,
            expiresAt = expiresAt,
            name = name,
            cost = cost,
            dock = dock,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary attempt digest in status requested. */
fun newPrimaryAttemptDigest(code: String, label: String): PrimaryAttemptDigest {
    return PrimaryAttemptDigest(
        code = code,
        label = label,
        visible = false,
        threshold = 0.0,
        approved = false,
        fuelLiters = 0.0,
        expiresAt = 0,
        name = "",
        cost = 0.0,
        dock = "",
        durationMinutes = 0,
        status = PrimaryAttemptDigestStatus.REQUESTED,
    )
}
