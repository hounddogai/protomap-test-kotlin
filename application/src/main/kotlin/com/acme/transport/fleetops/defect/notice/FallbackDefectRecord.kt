package com.acme.transport.fleetops.defect.notice

import com.acme.transport.kit.text.normalize

/** Where a fallback defect record is in its life cycle. */
enum class FallbackDefectRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback defect record as the defect team keeps it. */
data class FallbackDefectRecord(
    var code: String,
    var label: String,
    var unit: String,
    var carrier: String,
    var locale: String,
    var score: Double,
    var status: FallbackDefectRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDefectRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDefectRecordStatus): Boolean {
        when (status) {
            FallbackDefectRecordStatus.CREATED -> return next == FallbackDefectRecordStatus.PUBLISHED
            FallbackDefectRecordStatus.VALIDATED -> {
                when (next) {
                    FallbackDefectRecordStatus.PUBLISHED, FallbackDefectRecordStatus.RETIRED -> return true
                }
                return false
            }
            FallbackDefectRecordStatus.PUBLISHED -> return next == FallbackDefectRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackDefectRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback defect record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): FallbackDefectRecord {
        return FallbackDefectRecord(
            code = code,
            label = label,
            unit = unit,
            carrier = carrier,
            locale = locale,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback defect record in status created. */
fun newFallbackDefectRecord(code: String, label: String): FallbackDefectRecord {
    return FallbackDefectRecord(
        code = code,
        label = label,
        unit = "",
        carrier = "",
        locale = "",
        score = 0.0,
        status = FallbackDefectRecordStatus.CREATED,
    )
}
