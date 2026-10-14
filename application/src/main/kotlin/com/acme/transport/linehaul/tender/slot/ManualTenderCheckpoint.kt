package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tender checkpoint is in its life cycle. */
enum class ManualTenderCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual tender checkpoint. Its code is unique within a tenant. */
data class ManualTenderCheckpoint(
    var code: String,
    var label: String,
    var contactEmail: String,
    var segment: String,
    var pallets: Int,
    var budget: Double,
    var summary: String,
    var unit: String,
    var status: ManualTenderCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTenderCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTenderCheckpointStatus): Boolean {
        when (status) {
            ManualTenderCheckpointStatus.CREATED -> return next == ManualTenderCheckpointStatus.PUBLISHED
            ManualTenderCheckpointStatus.VALIDATED -> {
                when (next) {
                    ManualTenderCheckpointStatus.PUBLISHED, ManualTenderCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            ManualTenderCheckpointStatus.PUBLISHED -> {
                when (next) {
                    ManualTenderCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTenderCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("manual tender checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual tender checkpoint"} $code"

    fun adjustedExposure(): Double = round(budget * pallets.toDouble() * 100) / 100

    fun snapshot(): ManualTenderCheckpoint {
        return ManualTenderCheckpoint(
            code = code,
            label = label,
            contactEmail = contactEmail,
            segment = segment,
            pallets = pallets,
            budget = budget,
            summary = summary,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual tender checkpoint in status created. */
fun newManualTenderCheckpoint(code: String, label: String): ManualTenderCheckpoint {
    return ManualTenderCheckpoint(
        code = code,
        label = label,
        contactEmail = "",
        segment = "",
        pallets = 0,
        budget = 0.0,
        summary = "",
        unit = "",
        status = ManualTenderCheckpointStatus.CREATED,
    )
}
