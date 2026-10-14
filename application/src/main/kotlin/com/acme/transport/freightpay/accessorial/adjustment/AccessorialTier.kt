package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial tier is in its life cycle. */
enum class AccessorialTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a accessorial tier and its current status. */
data class AccessorialTier(
    var code: String,
    var label: String,
    var ownerId: String,
    var carrier: String,
    var etaMinutes: Int,
    var shiftCode: String,
    var capacity: Int,
    var status: AccessorialTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialTierStatus): Boolean {
        when (status) {
            AccessorialTierStatus.PLANNED -> return next == AccessorialTierStatus.SCHEDULED || next == AccessorialTierStatus.CANCELLED
            AccessorialTierStatus.SCHEDULED -> return next == AccessorialTierStatus.COMPLETED || next == AccessorialTierStatus.CANCELLED
            AccessorialTierStatus.IN_PROGRESS -> {
                when (next) {
                    AccessorialTierStatus.COMPLETED -> return true
                }
                return false
            }
            AccessorialTierStatus.COMPLETED -> return next == AccessorialTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AccessorialTierStatus) {
        if (!canMoveTo(next)) {
            error("accessorial tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round(etaMinutes.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): AccessorialTier {
        return AccessorialTier(
            code = code,
            label = label,
            ownerId = ownerId,
            carrier = carrier,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a accessorial tier in status planned. */
fun newAccessorialTier(code: String, label: String): AccessorialTier {
    return AccessorialTier(
        code = code,
        label = label,
        ownerId = "",
        carrier = "",
        etaMinutes = 0,
        shiftCode = "",
        capacity = 0,
        status = AccessorialTierStatus.PLANNED,
    )
}
