package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy locker audit is in its life cycle. */
enum class LegacyLockerAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a legacy locker audit and its current status. */
data class LegacyLockerAudit(
    var code: String,
    var name: String,
    var graceDays: Int,
    var category: String,
    var fuelLiters: Double,
    var distanceKm: Double,
    var status: LegacyLockerAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLockerAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLockerAuditStatus): Boolean {
        when (status) {
            LegacyLockerAuditStatus.IDLE -> {
                when (next) {
                    LegacyLockerAuditStatus.WARMING, LegacyLockerAuditStatus.DRAINING -> return true
                }
                return false
            }
            LegacyLockerAuditStatus.WARMING -> return next == LegacyLockerAuditStatus.READY || next == LegacyLockerAuditStatus.STOPPED
            LegacyLockerAuditStatus.READY -> return next == LegacyLockerAuditStatus.DRAINING || next == LegacyLockerAuditStatus.STOPPED
            LegacyLockerAuditStatus.DRAINING -> return next == LegacyLockerAuditStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyLockerAuditStatus) {
        if (!canMoveTo(next)) {
            error("legacy locker audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy locker audit"} $code"

    fun effectiveExposure(): Double = round(distanceKm * fuelLiters * 100) / 100

    fun snapshot(): LegacyLockerAudit {
        return LegacyLockerAudit(
            code = code,
            name = name,
            graceDays = graceDays,
            category = category,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy locker audit in status idle. */
fun newLegacyLockerAudit(code: String, name: String): LegacyLockerAudit {
    return LegacyLockerAudit(
        code = code,
        name = name,
        graceDays = 0,
        category = "",
        fuelLiters = 0.0,
        distanceKm = 0.0,
        status = LegacyLockerAuditStatus.IDLE,
    )
}
