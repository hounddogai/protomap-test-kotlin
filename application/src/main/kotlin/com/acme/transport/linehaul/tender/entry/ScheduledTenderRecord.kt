package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled tender record is in its life cycle. */
enum class ScheduledTenderRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled tender record, with what the planners need to act on it. */
data class ScheduledTenderRecord(
    var code: String,
    var title: String,
    var sequence: Int,
    var vehicleId: String,
    var budget: Double,
    var createdAt: Int,
    var name: String,
    var status: ScheduledTenderRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTenderRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTenderRecordStatus): Boolean {
        when (status) {
            ScheduledTenderRecordStatus.IDLE -> {
                when (next) {
                    ScheduledTenderRecordStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledTenderRecordStatus.WARMING -> {
                when (next) {
                    ScheduledTenderRecordStatus.READY -> return true
                }
                return false
            }
            ScheduledTenderRecordStatus.READY -> return next == ScheduledTenderRecordStatus.DRAINING || next == ScheduledTenderRecordStatus.STOPPED
            ScheduledTenderRecordStatus.DRAINING -> return next == ScheduledTenderRecordStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledTenderRecordStatus) {
        if (!canMoveTo(next)) {
            error("scheduled tender record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((createdAt.toDouble() - budget) * 100) / 100

    fun snapshot(): ScheduledTenderRecord {
        return ScheduledTenderRecord(
            code = code,
            title = title,
            sequence = sequence,
            vehicleId = vehicleId,
            budget = budget,
            createdAt = createdAt,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled tender record in status idle. */
fun newScheduledTenderRecord(code: String, title: String): ScheduledTenderRecord {
    return ScheduledTenderRecord(
        code = code,
        title = title,
        sequence = 0,
        vehicleId = "",
        budget = 0.0,
        createdAt = 0,
        name = "",
        status = ScheduledTenderRecordStatus.IDLE,
    )
}
