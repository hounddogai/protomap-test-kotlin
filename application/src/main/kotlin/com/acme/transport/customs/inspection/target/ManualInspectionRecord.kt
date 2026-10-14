package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual inspection record is in its life cycle. */
enum class ManualInspectionRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a manual inspection record and its current status. */
data class ManualInspectionRecord(
    var code: String,
    var name: String,
    var driverId: String,
    var region: String,
    var createdAt: Int,
    var endsAt: Int,
    var version: Int,
    var reviewer: String,
    var distanceKm: Double,
    var windowDays: Int,
    var lane: String,
    var status: ManualInspectionRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualInspectionRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualInspectionRecordStatus): Boolean {
        when (status) {
            ManualInspectionRecordStatus.PROPOSED -> return next == ManualInspectionRecordStatus.APPROVED
            ManualInspectionRecordStatus.APPROVED -> {
                when (next) {
                    ManualInspectionRecordStatus.REJECTED, ManualInspectionRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualInspectionRecordStatus.REJECTED -> {
                when (next) {
                    ManualInspectionRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualInspectionRecordStatus) {
        if (!canMoveTo(next)) {
            error("manual inspection record $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round((version.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): ManualInspectionRecord {
        return ManualInspectionRecord(
            code = code,
            name = name,
            driverId = driverId,
            region = region,
            createdAt = createdAt,
            endsAt = endsAt,
            version = version,
            reviewer = reviewer,
            distanceKm = distanceKm,
            windowDays = windowDays,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual inspection record in status proposed. */
fun newManualInspectionRecord(code: String, name: String): ManualInspectionRecord {
    return ManualInspectionRecord(
        code = code,
        name = name,
        driverId = "",
        region = "",
        createdAt = 0,
        endsAt = 0,
        version = 0,
        reviewer = "",
        distanceKm = 0.0,
        windowDays = 0,
        lane = "",
        status = ManualInspectionRecordStatus.PROPOSED,
    )
}
