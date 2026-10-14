package com.acme.transport.terminal.sortation.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy sortation record is in its life cycle. */
enum class LegacySortationRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy sortation record. Its code is unique within a tenant. */
data class LegacySortationRecord(
    var code: String,
    var label: String,
    var reviewer: String,
    var visible: Boolean,
    var category: String,
    var retries: Int,
    var timezone: String,
    var rate: Double,
    var cost: Double,
    var status: LegacySortationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySortationRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySortationRecordStatus): Boolean {
        when (status) {
            LegacySortationRecordStatus.IDLE -> return next == LegacySortationRecordStatus.WARMING
            LegacySortationRecordStatus.WARMING -> return next == LegacySortationRecordStatus.DRAINING || next == LegacySortationRecordStatus.STOPPED
            LegacySortationRecordStatus.READY -> return next == LegacySortationRecordStatus.STOPPED
            LegacySortationRecordStatus.DRAINING -> {
                when (next) {
                    LegacySortationRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacySortationRecordStatus) {
        if (!canMoveTo(next)) {
            error("legacy sortation record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy sortation record"} $code"

    fun projectedVolume(): Double = round(retries.toDouble() * rate * 100) / 100

    fun snapshot(): LegacySortationRecord {
        return LegacySortationRecord(
            code = code,
            label = label,
            reviewer = reviewer,
            visible = visible,
            category = category,
            retries = retries,
            timezone = timezone,
            rate = rate,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a legacy sortation record in status idle. */
fun newLegacySortationRecord(code: String, label: String): LegacySortationRecord {
    return LegacySortationRecord(
        code = code,
        label = label,
        reviewer = "",
        visible = false,
        category = "",
        retries = 0,
        timezone = "",
        rate = 0.0,
        cost = 0.0,
        status = LegacySortationRecordStatus.IDLE,
    )
}
