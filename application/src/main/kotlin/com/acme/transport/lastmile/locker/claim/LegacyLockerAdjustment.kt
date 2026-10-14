package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy locker adjustment is in its life cycle. */
enum class LegacyLockerAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a legacy locker adjustment and its current status. */
data class LegacyLockerAdjustment(
    var code: String,
    var title: String,
    var limit: Int,
    var summary: String,
    var notes: String,
    var durationMinutes: Int,
    var status: LegacyLockerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLockerAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLockerAdjustmentStatus): Boolean {
        when (status) {
            LegacyLockerAdjustmentStatus.REQUESTED -> return next == LegacyLockerAdjustmentStatus.GRANTED
            LegacyLockerAdjustmentStatus.GRANTED -> {
                when (next) {
                    LegacyLockerAdjustmentStatus.REVOKED, LegacyLockerAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyLockerAdjustmentStatus.REVOKED -> return next == LegacyLockerAdjustmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LegacyLockerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("legacy locker adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy locker adjustment"} $code"

    fun netExposure(): Double = round((durationMinutes.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): LegacyLockerAdjustment {
        return LegacyLockerAdjustment(
            code = code,
            title = title,
            limit = limit,
            summary = summary,
            notes = notes,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy locker adjustment in status requested. */
fun newLegacyLockerAdjustment(code: String, title: String): LegacyLockerAdjustment {
    return LegacyLockerAdjustment(
        code = code,
        title = title,
        limit = 0,
        summary = "",
        notes = "",
        durationMinutes = 0,
        status = LegacyLockerAdjustmentStatus.REQUESTED,
    )
}
