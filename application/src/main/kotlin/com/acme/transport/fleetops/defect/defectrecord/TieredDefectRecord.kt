package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered defect record records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a tiered defect record is in its life cycle. */
enum class TieredDefectRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered defect record. Its code is unique within a tenant. */
data class TieredDefectRecord(
    var code: String,
    var title: String,
    var category: String,
    var source: String,
    var expiresAt: Int,
    var sequence: Int,
    var distanceKm: Double,
    var graceDays: Int,
    var status: TieredDefectRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDefectRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDefectRecordStatus): Boolean {
        when (status) {
            TieredDefectRecordStatus.CREATED -> return next == TieredDefectRecordStatus.PUBLISHED || next == TieredDefectRecordStatus.RETIRED
            TieredDefectRecordStatus.VALIDATED -> {
                when (next) {
                    TieredDefectRecordStatus.RETIRED -> return true
                }
                return false
            }
            TieredDefectRecordStatus.PUBLISHED -> return next == TieredDefectRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredDefectRecordStatus) {
        if (!canMoveTo(next)) {
            error("tiered defect record $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered defect record"} $code"

    fun adjustedVolume(): Double = round((distanceKm - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TieredDefectRecord {
        return TieredDefectRecord(
            code = code,
            title = title,
            category = category,
            source = source,
            expiresAt = expiresAt,
            sequence = sequence,
            distanceKm = distanceKm,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered defect record in status created. */
fun newTieredDefectRecord(code: String, title: String): TieredDefectRecord {
    return TieredDefectRecord(
        code = code,
        title = title,
        category = "",
        source = "",
        expiresAt = 0,
        sequence = 0,
        distanceKm = 0.0,
        graceDays = 0,
        status = TieredDefectRecordStatus.CREATED,
    )
}
