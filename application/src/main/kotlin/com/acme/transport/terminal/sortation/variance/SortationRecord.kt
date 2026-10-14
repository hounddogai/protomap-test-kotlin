package com.acme.transport.terminal.sortation.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation record is in its life cycle. */
enum class SortationRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One sortation record, with what the planners need to act on it. */
data class SortationRecord(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var approved: Boolean,
    var startsAt: Int,
    var retries: Int,
    var sequence: Int,
    var minimum: Double,
    var status: SortationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationRecordStatus): Boolean {
        when (status) {
            SortationRecordStatus.IDLE -> return next == SortationRecordStatus.STOPPED
            SortationRecordStatus.WARMING -> return next == SortationRecordStatus.STOPPED
            SortationRecordStatus.READY -> {
                when (next) {
                    SortationRecordStatus.DRAINING -> return true
                }
                return false
            }
            SortationRecordStatus.DRAINING -> return next == SortationRecordStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SortationRecordStatus) {
        if (!canMoveTo(next)) {
            error("sortation record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedLoad(): Double = round((fuelLiters - startsAt.toDouble()) * 100) / 100

    fun snapshot(): SortationRecord {
        return SortationRecord(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            approved = approved,
            startsAt = startsAt,
            retries = retries,
            sequence = sequence,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation record in status idle. */
fun newSortationRecord(code: String, label: String): SortationRecord {
    return SortationRecord(
        code = code,
        label = label,
        fuelLiters = 0.0,
        approved = false,
        startsAt = 0,
        retries = 0,
        sequence = 0,
        minimum = 0.0,
        status = SortationRecordStatus.IDLE,
    )
}
