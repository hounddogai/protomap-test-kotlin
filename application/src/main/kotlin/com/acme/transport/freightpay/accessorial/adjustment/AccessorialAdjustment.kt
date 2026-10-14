package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial adjustment records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a accessorial adjustment is in its life cycle. */
enum class AccessorialAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A accessorial adjustment. Its code is unique within a tenant. */
data class AccessorialAdjustment(
    var code: String,
    var title: String,
    var startsAt: Int,
    var driverId: String,
    var quantity: Int,
    var attempts: Int,
    var archived: Boolean,
    var checksum: String,
    var ratio: Double,
    var maximum: Double,
    var shiftCode: String,
    var status: AccessorialAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialAdjustmentStatus): Boolean {
        when (status) {
            AccessorialAdjustmentStatus.REQUESTED -> {
                when (next) {
                    AccessorialAdjustmentStatus.REVOKED, AccessorialAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            AccessorialAdjustmentStatus.GRANTED -> return next == AccessorialAdjustmentStatus.REVOKED
            AccessorialAdjustmentStatus.REVOKED -> return next == AccessorialAdjustmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AccessorialAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("accessorial adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"accessorial adjustment"} $code"

    fun netValue(): Double = round(ratio * attempts.toDouble() * 100) / 100

    fun snapshot(): AccessorialAdjustment {
        return AccessorialAdjustment(
            code = code,
            title = title,
            startsAt = startsAt,
            driverId = driverId,
            quantity = quantity,
            attempts = attempts,
            archived = archived,
            checksum = checksum,
            ratio = ratio,
            maximum = maximum,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a accessorial adjustment in status requested. */
fun newAccessorialAdjustment(code: String, title: String): AccessorialAdjustment {
    return AccessorialAdjustment(
        code = code,
        title = title,
        startsAt = 0,
        driverId = "",
        quantity = 0,
        attempts = 0,
        archived = false,
        checksum = "",
        ratio = 0.0,
        maximum = 0.0,
        shiftCode = "",
        status = AccessorialAdjustmentStatus.REQUESTED,
    )
}
