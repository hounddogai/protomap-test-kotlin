package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending hold slot is in its life cycle. */
enum class PendingHoldSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending hold slot as the hold team keeps it. */
data class PendingHoldSlot(
    var code: String,
    var title: String,
    var ceiling: Double,
    var createdAt: Int,
    var carrier: String,
    var unit: String,
    var loadKg: Double,
    var name: String,
    var contactEmail: String,
    var segment: String,
    var status: PendingHoldSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldSlotStatus): Boolean {
        when (status) {
            PendingHoldSlotStatus.PLANNED -> {
                when (next) {
                    PendingHoldSlotStatus.SCHEDULED, PendingHoldSlotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            PendingHoldSlotStatus.SCHEDULED -> return next == PendingHoldSlotStatus.CANCELLED
            PendingHoldSlotStatus.IN_PROGRESS -> return next == PendingHoldSlotStatus.COMPLETED
            PendingHoldSlotStatus.COMPLETED -> return next == PendingHoldSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingHoldSlotStatus) {
        if (!canMoveTo(next)) {
            error("pending hold slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending hold slot"} $code"

    fun netLoad(): Double = round(loadKg * ceiling * 100) / 100

    fun snapshot(): PendingHoldSlot {
        return PendingHoldSlot(
            code = code,
            title = title,
            ceiling = ceiling,
            createdAt = createdAt,
            carrier = carrier,
            unit = unit,
            loadKg = loadKg,
            name = name,
            contactEmail = contactEmail,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending hold slot in status planned. */
fun newPendingHoldSlot(code: String, title: String): PendingHoldSlot {
    return PendingHoldSlot(
        code = code,
        title = title,
        ceiling = 0.0,
        createdAt = 0,
        carrier = "",
        unit = "",
        loadKg = 0.0,
        name = "",
        contactEmail = "",
        segment = "",
        status = PendingHoldSlotStatus.PLANNED,
    )
}
