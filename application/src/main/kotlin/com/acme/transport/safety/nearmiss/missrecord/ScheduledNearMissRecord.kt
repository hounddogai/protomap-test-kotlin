package com.acme.transport.safety.nearmiss.missrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled near miss record records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a scheduled near miss record is in its life cycle. */
enum class ScheduledNearMissRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scheduled near miss record and its current status. */
data class ScheduledNearMissRecord(
    var code: String,
    var label: String,
    var lane: String,
    var expiresAt: Int,
    var timezone: String,
    var region: String,
    var budget: Double,
    var rate: Double,
    var status: ScheduledNearMissRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNearMissRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNearMissRecordStatus): Boolean {
        when (status) {
            ScheduledNearMissRecordStatus.PROPOSED -> return next == ScheduledNearMissRecordStatus.APPROVED
            ScheduledNearMissRecordStatus.APPROVED -> return next == ScheduledNearMissRecordStatus.REJECTED
            ScheduledNearMissRecordStatus.REJECTED -> {
                when (next) {
                    ScheduledNearMissRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledNearMissRecordStatus) {
        if (!canMoveTo(next)) {
            error("scheduled near miss record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveExposure(): Double = round((budget + rate) * 100) / 100

    fun snapshot(): ScheduledNearMissRecord {
        return ScheduledNearMissRecord(
            code = code,
            label = label,
            lane = lane,
            expiresAt = expiresAt,
            timezone = timezone,
            region = region,
            budget = budget,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled near miss record in status proposed. */
fun newScheduledNearMissRecord(code: String, label: String): ScheduledNearMissRecord {
    return ScheduledNearMissRecord(
        code = code,
        label = label,
        lane = "",
        expiresAt = 0,
        timezone = "",
        region = "",
        budget = 0.0,
        rate = 0.0,
        status = ScheduledNearMissRecordStatus.PROPOSED,
    )
}
