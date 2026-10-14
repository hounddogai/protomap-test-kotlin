package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.text.normalize

// The capped inspection slot records of customs's inspection work, and the rules, stores, and calculations
// around them.

/** Where a capped inspection slot is in its life cycle. */
enum class CappedInspectionSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped inspection slot as the inspection team keeps it. */
data class CappedInspectionSlot(
    var code: String,
    var name: String,
    var title: String,
    var pinned: Boolean,
    var country: String,
    var score: Double,
    var status: CappedInspectionSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionSlotStatus): Boolean {
        when (status) {
            CappedInspectionSlotStatus.REQUESTED -> return next == CappedInspectionSlotStatus.GRANTED || next == CappedInspectionSlotStatus.REVOKED
            CappedInspectionSlotStatus.GRANTED -> return next == CappedInspectionSlotStatus.REVOKED
            CappedInspectionSlotStatus.REVOKED -> {
                when (next) {
                    CappedInspectionSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedInspectionSlotStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = score

    fun snapshot(): CappedInspectionSlot {
        return CappedInspectionSlot(
            code = code,
            name = name,
            title = title,
            pinned = pinned,
            country = country,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped inspection slot in status requested. */
fun newCappedInspectionSlot(code: String, name: String): CappedInspectionSlot {
    return CappedInspectionSlot(
        code = code,
        name = name,
        title = "",
        pinned = false,
        country = "",
        score = 0.0,
        status = CappedInspectionSlotStatus.REQUESTED,
    )
}
