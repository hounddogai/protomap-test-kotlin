package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection slot is in its life cycle. */
enum class InspectionSlotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One inspection slot, with what the planners need to act on it. */
data class InspectionSlot(
    var code: String,
    var name: String,
    var capacity: Int,
    var createdAt: Int,
    var loadKg: Double,
    var status: InspectionSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionSlotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionSlotStatus): Boolean {
        when (status) {
            InspectionSlotStatus.CREATED -> {
                when (next) {
                    InspectionSlotStatus.VALIDATED -> return true
                }
                return false
            }
            InspectionSlotStatus.VALIDATED -> return next == InspectionSlotStatus.RETIRED
            InspectionSlotStatus.PUBLISHED -> return next == InspectionSlotStatus.RETIRED
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

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((loadKg - createdAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionSlot {
        return InspectionSlot(
            code = code,
            name = name,
            capacity = capacity,
            createdAt = createdAt,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection slot in status created. */
fun newInspectionSlot(code: String, name: String): InspectionSlot {
    return InspectionSlot(
        code = code,
        name = name,
        capacity = 0,
        createdAt = 0,
        loadKg = 0.0,
        status = InspectionSlotStatus.CREATED,
    )
}
