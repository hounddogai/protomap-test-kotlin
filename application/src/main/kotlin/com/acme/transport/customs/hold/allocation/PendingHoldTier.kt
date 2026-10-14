package com.acme.transport.customs.hold.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hold tier is in its life cycle. */
enum class PendingHoldTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending hold tier, with what the planners need to act on it. */
data class PendingHoldTier(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var timezone: String,
    var locale: String,
    var shiftCode: String,
    var vehicleId: String,
    var capacity: Int,
    var etaMinutes: Int,
    var status: PendingHoldTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldTierStatus): Boolean {
        when (status) {
            PendingHoldTierStatus.PLANNED -> {
                when (next) {
                    PendingHoldTierStatus.SCHEDULED, PendingHoldTierStatus.CANCELLED -> return true
                }
                return false
            }
            PendingHoldTierStatus.SCHEDULED -> return next == PendingHoldTierStatus.COMPLETED || next == PendingHoldTierStatus.CANCELLED
            PendingHoldTierStatus.IN_PROGRESS -> {
                when (next) {
                    PendingHoldTierStatus.COMPLETED -> return true
                }
                return false
            }
            PendingHoldTierStatus.COMPLETED -> {
                when (next) {
                    PendingHoldTierStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingHoldTierStatus) {
        if (!canMoveTo(next)) {
            error("pending hold tier $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round(etaMinutes.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): PendingHoldTier {
        return PendingHoldTier(
            code = code,
            label = label,
            expiresAt = expiresAt,
            timezone = timezone,
            locale = locale,
            shiftCode = shiftCode,
            vehicleId = vehicleId,
            capacity = capacity,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a pending hold tier in status planned. */
fun newPendingHoldTier(code: String, label: String): PendingHoldTier {
    return PendingHoldTier(
        code = code,
        label = label,
        expiresAt = 0,
        timezone = "",
        locale = "",
        shiftCode = "",
        vehicleId = "",
        capacity = 0,
        etaMinutes = 0,
        status = PendingHoldTierStatus.PLANNED,
    )
}
