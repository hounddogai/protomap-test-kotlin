package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection record is in its life cycle. */
enum class LegacyInspectionRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy inspection record. Its code is unique within a tenant. */
data class LegacyInspectionRecord(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var shiftCode: String,
    var name: String,
    var unit: String,
    var stopCount: Int,
    var revision: Int,
    var floorValue: Double,
    var status: LegacyInspectionRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionRecordStatus): Boolean {
        when (status) {
            LegacyInspectionRecordStatus.PROPOSED -> {
                when (next) {
                    LegacyInspectionRecordStatus.APPROVED, LegacyInspectionRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyInspectionRecordStatus.APPROVED -> return next == LegacyInspectionRecordStatus.REJECTED || next == LegacyInspectionRecordStatus.WITHDRAWN
            LegacyInspectionRecordStatus.REJECTED -> return next == LegacyInspectionRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyInspectionRecordStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection record $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"legacy inspection record"} $code"

    fun adjustedValue(): Double = round((stopCount.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): LegacyInspectionRecord {
        return LegacyInspectionRecord(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            name = name,
            unit = unit,
            stopCount = stopCount,
            revision = revision,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy inspection record in status proposed. */
fun newLegacyInspectionRecord(code: String, label: String): LegacyInspectionRecord {
    return LegacyInspectionRecord(
        code = code,
        label = label,
        etaMinutes = 0,
        shiftCode = "",
        name = "",
        unit = "",
        stopCount = 0,
        revision = 0,
        floorValue = 0.0,
        status = LegacyInspectionRecordStatus.PROPOSED,
    )
}
