package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending defect slot is in its life cycle. */
enum class PendingDefectSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One pending defect slot, with what the planners need to act on it. */
data class PendingDefectSlot(
    var code: String,
    var label: String,
    var channel: String,
    var archived: Boolean,
    var quantity: Int,
    var floorValue: Double,
    var checksum: String,
    var name: String,
    var minimum: Double,
    var distanceKm: Double,
    var budget: Double,
    var status: PendingDefectSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDefectSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDefectSlotStatus): Boolean {
        when (status) {
            PendingDefectSlotStatus.DRAFT -> return next == PendingDefectSlotStatus.PENDING || next == PendingDefectSlotStatus.SUSPENDED
            PendingDefectSlotStatus.PENDING -> return next == PendingDefectSlotStatus.SUSPENDED
            PendingDefectSlotStatus.ACTIVE -> return next == PendingDefectSlotStatus.SUSPENDED || next == PendingDefectSlotStatus.CLOSED
            PendingDefectSlotStatus.SUSPENDED -> {
                when (next) {
                    PendingDefectSlotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDefectSlotStatus) {
        if (!canMoveTo(next)) {
            error("pending defect slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending defect slot"} $code"

    fun effectiveLoad(): Double = round((minimum + budget) * 100) / 100

    fun snapshot(): PendingDefectSlot {
        return PendingDefectSlot(
            code = code,
            label = label,
            channel = channel,
            archived = archived,
            quantity = quantity,
            floorValue = floorValue,
            checksum = checksum,
            name = name,
            minimum = minimum,
            distanceKm = distanceKm,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending defect slot in status draft. */
fun newPendingDefectSlot(code: String, label: String): PendingDefectSlot {
    return PendingDefectSlot(
        code = code,
        label = label,
        channel = "",
        archived = false,
        quantity = 0,
        floorValue = 0.0,
        checksum = "",
        name = "",
        minimum = 0.0,
        distanceKm = 0.0,
        budget = 0.0,
        status = PendingDefectSlotStatus.DRAFT,
    )
}
