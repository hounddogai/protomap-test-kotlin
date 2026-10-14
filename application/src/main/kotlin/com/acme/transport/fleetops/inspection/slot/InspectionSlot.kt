package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.kit.text.normalize

// The inspection slot records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection slot is in its life cycle. */
enum class InspectionSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a inspection slot and its current status. */
data class InspectionSlot(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var createdAt: Int,
    var label: String,
    var status: InspectionSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionSlotStatus): Boolean {
        when (status) {
            InspectionSlotStatus.PLANNED -> return next == InspectionSlotStatus.SCHEDULED || next == InspectionSlotStatus.CANCELLED
            InspectionSlotStatus.SCHEDULED -> {
                when (next) {
                    InspectionSlotStatus.COMPLETED -> return true
                }
                return false
            }
            InspectionSlotStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionSlotStatus.COMPLETED, InspectionSlotStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionSlotStatus.COMPLETED -> return next == InspectionSlotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: InspectionSlotStatus) {
        if (!canMoveTo(next)) {
            error("inspection slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): InspectionSlot {
        return InspectionSlot(
            code = code,
            name = name,
            tags = tags,
            createdAt = createdAt,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection slot in status planned. */
fun newInspectionSlot(code: String, name: String): InspectionSlot {
    return InspectionSlot(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        createdAt = 0,
        label = "",
        status = InspectionSlotStatus.PLANNED,
    )
}
