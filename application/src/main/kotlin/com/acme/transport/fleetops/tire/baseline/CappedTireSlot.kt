package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tire slot is in its life cycle. */
enum class CappedTireSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped tire slot. Its code is unique within a tenant. */
data class CappedTireSlot(
    var code: String,
    var label: String,
    var region: String,
    var batchSize: Int,
    var createdAt: Int,
    var priority: Int,
    var cost: Double,
    var carrier: String,
    var summary: String,
    var visible: Boolean,
    var pinned: Boolean,
    var status: CappedTireSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTireSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTireSlotStatus): Boolean {
        when (status) {
            CappedTireSlotStatus.PLANNED -> return next == CappedTireSlotStatus.SCHEDULED || next == CappedTireSlotStatus.COMPLETED
            CappedTireSlotStatus.SCHEDULED -> {
                when (next) {
                    CappedTireSlotStatus.COMPLETED -> return true
                }
                return false
            }
            CappedTireSlotStatus.IN_PROGRESS -> return next == CappedTireSlotStatus.COMPLETED
            CappedTireSlotStatus.COMPLETED -> return next == CappedTireSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedTireSlotStatus) {
        if (!canMoveTo(next)) {
            error("capped tire slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped tire slot"} $code"

    fun projectedVolume(): Double = round(createdAt.toDouble() * cost * 100) / 100

    fun snapshot(): CappedTireSlot {
        return CappedTireSlot(
            code = code,
            label = label,
            region = region,
            batchSize = batchSize,
            createdAt = createdAt,
            priority = priority,
            cost = cost,
            carrier = carrier,
            summary = summary,
            visible = visible,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped tire slot in status planned. */
fun newCappedTireSlot(code: String, label: String): CappedTireSlot {
    return CappedTireSlot(
        code = code,
        label = label,
        region = "",
        batchSize = 0,
        createdAt = 0,
        priority = 0,
        cost = 0.0,
        carrier = "",
        summary = "",
        visible = false,
        pinned = false,
        status = CappedTireSlotStatus.PLANNED,
    )
}
