package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection schedule is in its life cycle. */
enum class LegacyInspectionScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy inspection schedule. Its code is unique within a tenant. */
data class LegacyInspectionSchedule(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var phone: String,
    var pallets: Int,
    var etaMinutes: Int,
    var unit: String,
    var status: LegacyInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionScheduleStatus): Boolean {
        when (status) {
            LegacyInspectionScheduleStatus.REQUESTED -> {
                when (next) {
                    LegacyInspectionScheduleStatus.GRANTED -> return true
                }
                return false
            }
            LegacyInspectionScheduleStatus.GRANTED -> {
                when (next) {
                    LegacyInspectionScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyInspectionScheduleStatus.REVOKED -> {
                when (next) {
                    LegacyInspectionScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy inspection schedule"} $code"

    fun weightedValue(): Double = round((etaMinutes.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): LegacyInspectionSchedule {
        return LegacyInspectionSchedule(
            code = code,
            name = name,
            distanceKm = distanceKm,
            phone = phone,
            pallets = pallets,
            etaMinutes = etaMinutes,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a legacy inspection schedule in status requested. */
fun newLegacyInspectionSchedule(code: String, name: String): LegacyInspectionSchedule {
    return LegacyInspectionSchedule(
        code = code,
        name = name,
        distanceKm = 0.0,
        phone = "",
        pallets = 0,
        etaMinutes = 0,
        unit = "",
        status = LegacyInspectionScheduleStatus.REQUESTED,
    )
}
