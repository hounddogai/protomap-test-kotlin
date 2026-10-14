package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy tender record records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a legacy tender record is in its life cycle. */
enum class LegacyTenderRecordStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a legacy tender record and its current status. */
data class LegacyTenderRecord(
    var code: String,
    var title: String,
    var graceDays: Int,
    var version: Int,
    var checksum: String,
    var status: LegacyTenderRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTenderRecordStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTenderRecordStatus): Boolean {
        when (status) {
            LegacyTenderRecordStatus.PLANNED -> return next == LegacyTenderRecordStatus.COMPLETED || next == LegacyTenderRecordStatus.CANCELLED
            LegacyTenderRecordStatus.SCHEDULED -> {
                when (next) {
                    LegacyTenderRecordStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyTenderRecordStatus.IN_PROGRESS -> return next == LegacyTenderRecordStatus.COMPLETED || next == LegacyTenderRecordStatus.CANCELLED
            LegacyTenderRecordStatus.COMPLETED -> {
                when (next) {
                    LegacyTenderRecordStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTenderRecordStatus) {
        if (!canMoveTo(next)) {
            error("legacy tender record $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedLoad(): Double = round((graceDays.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): LegacyTenderRecord {
        return LegacyTenderRecord(
            code = code,
            title = title,
            graceDays = graceDays,
            version = version,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy tender record in status planned. */
fun newLegacyTenderRecord(code: String, title: String): LegacyTenderRecord {
    return LegacyTenderRecord(
        code = code,
        title = title,
        graceDays = 0,
        version = 0,
        checksum = "",
        status = LegacyTenderRecordStatus.PLANNED,
    )
}
