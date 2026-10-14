package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual duty period slot is in its life cycle. */
enum class ManualDutyPeriodSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual duty period slot. Its code is unique within a tenant. */
data class ManualDutyPeriodSlot(
    var code: String,
    var label: String,
    var vehicleId: String,
    var revision: Int,
    var floorValue: Double,
    var distanceKm: Double,
    var unit: String,
    var attempts: Int,
    var status: ManualDutyPeriodSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodSlotStatus): Boolean {
        when (status) {
            ManualDutyPeriodSlotStatus.REQUESTED -> {
                when (next) {
                    ManualDutyPeriodSlotStatus.EXPIRED -> return true
                }
                return false
            }
            ManualDutyPeriodSlotStatus.GRANTED -> {
                when (next) {
                    ManualDutyPeriodSlotStatus.REVOKED, ManualDutyPeriodSlotStatus.EXPIRED -> return true
                }
                return false
            }
            ManualDutyPeriodSlotStatus.REVOKED -> {
                when (next) {
                    ManualDutyPeriodSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodSlotStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period slot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round(revision.toDouble() * floorValue * 100) / 100

    fun snapshot(): ManualDutyPeriodSlot {
        return ManualDutyPeriodSlot(
            code = code,
            label = label,
            vehicleId = vehicleId,
            revision = revision,
            floorValue = floorValue,
            distanceKm = distanceKm,
            unit = unit,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual duty period slot in status requested. */
fun newManualDutyPeriodSlot(code: String, label: String): ManualDutyPeriodSlot {
    return ManualDutyPeriodSlot(
        code = code,
        label = label,
        vehicleId = "",
        revision = 0,
        floorValue = 0.0,
        distanceKm = 0.0,
        unit = "",
        attempts = 0,
        status = ManualDutyPeriodSlotStatus.REQUESTED,
    )
}
