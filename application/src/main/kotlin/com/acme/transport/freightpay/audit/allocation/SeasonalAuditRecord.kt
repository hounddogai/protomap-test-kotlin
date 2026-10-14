package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal audit record is in its life cycle. */
enum class SeasonalAuditRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One seasonal audit record, with what the planners need to act on it. */
data class SeasonalAuditRecord(
    var code: String,
    var title: String,
    var carrier: String,
    var checksum: String,
    var floorValue: Double,
    var notes: String,
    var durationMinutes: Int,
    var shiftCode: String,
    var status: SeasonalAuditRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAuditRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAuditRecordStatus): Boolean {
        when (status) {
            SeasonalAuditRecordStatus.CREATED -> return next == SeasonalAuditRecordStatus.VALIDATED || next == SeasonalAuditRecordStatus.PUBLISHED
            SeasonalAuditRecordStatus.VALIDATED -> return next == SeasonalAuditRecordStatus.PUBLISHED || next == SeasonalAuditRecordStatus.RETIRED
            SeasonalAuditRecordStatus.PUBLISHED -> {
                when (next) {
                    SeasonalAuditRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAuditRecordStatus) {
        if (!canMoveTo(next)) {
            error("seasonal audit record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round((durationMinutes.toDouble() + floorValue) * 100) / 100

    fun snapshot(): SeasonalAuditRecord {
        return SeasonalAuditRecord(
            code = code,
            title = title,
            carrier = carrier,
            checksum = checksum,
            floorValue = floorValue,
            notes = notes,
            durationMinutes = durationMinutes,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal audit record in status created. */
fun newSeasonalAuditRecord(code: String, title: String): SeasonalAuditRecord {
    return SeasonalAuditRecord(
        code = code,
        title = title,
        carrier = "",
        checksum = "",
        floorValue = 0.0,
        notes = "",
        durationMinutes = 0,
        shiftCode = "",
        status = SeasonalAuditRecordStatus.CREATED,
    )
}
