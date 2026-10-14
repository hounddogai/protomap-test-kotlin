package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker batch is in its life cycle. */
enum class LockerBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a locker batch and its current status. */
data class LockerBatch(
    var code: String,
    var label: String,
    var maximum: Double,
    var unit: String,
    var lane: String,
    var country: String,
    var capacity: Int,
    var updatedAt: Int,
    var version: Int,
    var timezone: String,
    var status: LockerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerBatchStatus): Boolean {
        when (status) {
            LockerBatchStatus.DRAFT -> {
                when (next) {
                    LockerBatchStatus.PENDING -> return true
                }
                return false
            }
            LockerBatchStatus.PENDING -> return next == LockerBatchStatus.ACTIVE || next == LockerBatchStatus.SUSPENDED
            LockerBatchStatus.ACTIVE -> {
                when (next) {
                    LockerBatchStatus.CLOSED -> return true
                }
                return false
            }
            LockerBatchStatus.SUSPENDED -> {
                when (next) {
                    LockerBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerBatchStatus) {
        if (!canMoveTo(next)) {
            error("locker batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"locker batch"} $code"

    fun adjustedVolume(): Double = round(maximum * updatedAt.toDouble() * 100) / 100

    fun snapshot(): LockerBatch {
        return LockerBatch(
            code = code,
            label = label,
            maximum = maximum,
            unit = unit,
            lane = lane,
            country = country,
            capacity = capacity,
            updatedAt = updatedAt,
            version = version,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a locker batch in status draft. */
fun newLockerBatch(code: String, label: String): LockerBatch {
    return LockerBatch(
        code = code,
        label = label,
        maximum = 0.0,
        unit = "",
        lane = "",
        country = "",
        capacity = 0,
        updatedAt = 0,
        version = 0,
        timezone = "",
        status = LockerBatchStatus.DRAFT,
    )
}
