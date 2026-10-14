package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped trailer record records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a capped trailer record is in its life cycle. */
enum class CappedTrailerRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One capped trailer record, with what the planners need to act on it. */
data class CappedTrailerRecord(
    var code: String,
    var label: String,
    var amount: Double,
    var version: Int,
    var notes: String,
    var ownerId: String,
    var endsAt: Int,
    var approved: Boolean,
    var createdAt: Int,
    var status: CappedTrailerRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTrailerRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTrailerRecordStatus): Boolean {
        when (status) {
            CappedTrailerRecordStatus.PLANNED -> return next == CappedTrailerRecordStatus.IN_PROGRESS
            CappedTrailerRecordStatus.SCHEDULED -> return next == CappedTrailerRecordStatus.IN_PROGRESS || next == CappedTrailerRecordStatus.COMPLETED
            CappedTrailerRecordStatus.IN_PROGRESS -> return next == CappedTrailerRecordStatus.COMPLETED || next == CappedTrailerRecordStatus.CANCELLED
            CappedTrailerRecordStatus.COMPLETED -> {
                when (next) {
                    CappedTrailerRecordStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTrailerRecordStatus) {
        if (!canMoveTo(next)) {
            error("capped trailer record $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveTotal(): Double = round((endsAt.toDouble() - amount) * 100) / 100

    fun snapshot(): CappedTrailerRecord {
        return CappedTrailerRecord(
            code = code,
            label = label,
            amount = amount,
            version = version,
            notes = notes,
            ownerId = ownerId,
            endsAt = endsAt,
            approved = approved,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped trailer record in status planned. */
fun newCappedTrailerRecord(code: String, label: String): CappedTrailerRecord {
    return CappedTrailerRecord(
        code = code,
        label = label,
        amount = 0.0,
        version = 0,
        notes = "",
        ownerId = "",
        endsAt = 0,
        approved = false,
        createdAt = 0,
        status = CappedTrailerRecordStatus.PLANNED,
    )
}
