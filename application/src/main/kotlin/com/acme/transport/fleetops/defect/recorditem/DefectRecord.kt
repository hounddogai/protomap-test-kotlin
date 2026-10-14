package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect record records of fleetops's defect work, and the rules, stores, and calculations around them.

/** Where a defect record is in its life cycle. */
enum class DefectRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A defect record as the defect team keeps it. */
data class DefectRecord(
    var code: String,
    var label: String,
    var cost: Double,
    var startsAt: Int,
    var durationMinutes: Int,
    var status: DefectRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectRecordStatus): Boolean {
        when (status) {
            DefectRecordStatus.CREATED -> {
                when (next) {
                    DefectRecordStatus.PUBLISHED, DefectRecordStatus.RETIRED -> return true
                }
                return false
            }
            DefectRecordStatus.VALIDATED -> {
                when (next) {
                    DefectRecordStatus.PUBLISHED, DefectRecordStatus.RETIRED -> return true
                }
                return false
            }
            DefectRecordStatus.PUBLISHED -> {
                when (next) {
                    DefectRecordStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectRecordStatus) {
        if (!canMoveTo(next)) {
            error("defect record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"defect record"} $code"

    fun effectiveExposure(): Double = round((cost + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefectRecord {
        return DefectRecord(
            code = code,
            label = label,
            cost = cost,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect record in status created. */
fun newDefectRecord(code: String, label: String): DefectRecord {
    return DefectRecord(
        code = code,
        label = label,
        cost = 0.0,
        startsAt = 0,
        durationMinutes = 0,
        status = DefectRecordStatus.CREATED,
    )
}
