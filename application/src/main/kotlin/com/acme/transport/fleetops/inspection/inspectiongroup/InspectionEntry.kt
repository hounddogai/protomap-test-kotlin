package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection entry is in its life cycle. */
enum class InspectionEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a inspection entry and its current status. */
data class InspectionEntry(
    var code: String,
    var label: String,
    var amount: Double,
    var loadKg: Double,
    var weight: Double,
    var status: InspectionEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionEntryStatus): Boolean {
        when (status) {
            InspectionEntryStatus.PLANNED -> return next == InspectionEntryStatus.CANCELLED
            InspectionEntryStatus.SCHEDULED -> return next == InspectionEntryStatus.IN_PROGRESS || next == InspectionEntryStatus.CANCELLED
            InspectionEntryStatus.IN_PROGRESS -> return next == InspectionEntryStatus.COMPLETED || next == InspectionEntryStatus.CANCELLED
            InspectionEntryStatus.COMPLETED -> {
                when (next) {
                    InspectionEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionEntryStatus) {
        if (!canMoveTo(next)) {
            error("inspection entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round(loadKg * amount * 100) / 100

    fun snapshot(): InspectionEntry {
        return InspectionEntry(
            code = code,
            label = label,
            amount = amount,
            loadKg = loadKg,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection entry in status planned. */
fun newInspectionEntry(code: String, label: String): InspectionEntry {
    return InspectionEntry(
        code = code,
        label = label,
        amount = 0.0,
        loadKg = 0.0,
        weight = 0.0,
        status = InspectionEntryStatus.PLANNED,
    )
}
