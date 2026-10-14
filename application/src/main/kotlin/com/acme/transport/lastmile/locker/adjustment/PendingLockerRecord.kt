package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending locker record is in its life cycle. */
enum class PendingLockerRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending locker record, with what the planners need to act on it. */
data class PendingLockerRecord(
    var code: String,
    var label: String,
    var name: String,
    var score: Double,
    var weight: Double,
    var unit: String,
    var approved: Boolean,
    var trailerId: String,
    var status: PendingLockerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLockerRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLockerRecordStatus): Boolean {
        when (status) {
            PendingLockerRecordStatus.CREATED -> {
                when (next) {
                    PendingLockerRecordStatus.VALIDATED -> return true
                }
                return false
            }
            PendingLockerRecordStatus.VALIDATED -> {
                when (next) {
                    PendingLockerRecordStatus.PUBLISHED, PendingLockerRecordStatus.RETIRED -> return true
                }
                return false
            }
            PendingLockerRecordStatus.PUBLISHED -> {
                when (next) {
                    PendingLockerRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingLockerRecordStatus) {
        if (!canMoveTo(next)) {
            error("pending locker record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedValue(): Double = round(score * weight * 100) / 100

    fun snapshot(): PendingLockerRecord {
        return PendingLockerRecord(
            code = code,
            label = label,
            name = name,
            score = score,
            weight = weight,
            unit = unit,
            approved = approved,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending locker record in status created. */
fun newPendingLockerRecord(code: String, label: String): PendingLockerRecord {
    return PendingLockerRecord(
        code = code,
        label = label,
        name = "",
        score = 0.0,
        weight = 0.0,
        unit = "",
        approved = false,
        trailerId = "",
        status = PendingLockerRecordStatus.CREATED,
    )
}
