package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection audit is in its life cycle. */
enum class AdaptiveInspectionAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A adaptive inspection audit as the inspection team keeps it. */
data class AdaptiveInspectionAudit(
    var code: String,
    var name: String,
    var minimum: Double,
    var unit: String,
    var shiftCode: String,
    var maximum: Double,
    var loadKg: Double,
    var fuelLiters: Double,
    var notes: String,
    var locale: String,
    var status: AdaptiveInspectionAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionAuditStatus): Boolean {
        when (status) {
            AdaptiveInspectionAuditStatus.PLANNED -> {
                when (next) {
                    AdaptiveInspectionAuditStatus.IN_PROGRESS, AdaptiveInspectionAuditStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveInspectionAuditStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveInspectionAuditStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveInspectionAuditStatus.IN_PROGRESS -> return next == AdaptiveInspectionAuditStatus.COMPLETED || next == AdaptiveInspectionAuditStatus.CANCELLED
            AdaptiveInspectionAuditStatus.COMPLETED -> return next == AdaptiveInspectionAuditStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionAuditStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round((minimum - maximum) * 100) / 100

    fun snapshot(): AdaptiveInspectionAudit {
        return AdaptiveInspectionAudit(
            code = code,
            name = name,
            minimum = minimum,
            unit = unit,
            shiftCode = shiftCode,
            maximum = maximum,
            loadKg = loadKg,
            fuelLiters = fuelLiters,
            notes = notes,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a adaptive inspection audit in status planned. */
fun newAdaptiveInspectionAudit(code: String, name: String): AdaptiveInspectionAudit {
    return AdaptiveInspectionAudit(
        code = code,
        name = name,
        minimum = 0.0,
        unit = "",
        shiftCode = "",
        maximum = 0.0,
        loadKg = 0.0,
        fuelLiters = 0.0,
        notes = "",
        locale = "",
        status = AdaptiveInspectionAuditStatus.PLANNED,
    )
}
