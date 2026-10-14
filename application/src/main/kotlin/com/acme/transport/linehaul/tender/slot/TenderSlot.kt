package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.text.normalize

// The tender slot records of linehaul's tender work, and the rules, stores, and calculations around them.

/** Where a tender slot is in its life cycle. */
enum class TenderSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tender slot. Its code is unique within a tenant. */
data class TenderSlot(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var depot: String,
    var score: Double,
    var status: TenderSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderSlotStatus): Boolean {
        when (status) {
            TenderSlotStatus.PROPOSED -> {
                when (next) {
                    TenderSlotStatus.REJECTED, TenderSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            TenderSlotStatus.APPROVED -> {
                when (next) {
                    TenderSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            TenderSlotStatus.REJECTED -> return next == TenderSlotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TenderSlotStatus) {
        if (!canMoveTo(next)) {
            error("tender slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): TenderSlot {
        return TenderSlot(
            code = code,
            title = title,
            attributes = attributes,
            notes = notes,
            depot = depot,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tender slot in status proposed. */
fun newTenderSlot(code: String, title: String): TenderSlot {
    return TenderSlot(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        notes = "",
        depot = "",
        score = 0.0,
        status = TenderSlotStatus.PROPOSED,
    )
}
