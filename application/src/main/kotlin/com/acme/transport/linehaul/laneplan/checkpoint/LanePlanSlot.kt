package com.acme.transport.linehaul.laneplan.checkpoint

import com.acme.transport.kit.text.normalize

/** Where a lane plan slot is in its life cycle. */
enum class LanePlanSlotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lane plan slot and its current status. */
data class LanePlanSlot(
    var code: String,
    var name: String,
    var reviewer: String,
    var locale: String,
    var discount: Double,
    var ownerId: String,
    var status: LanePlanSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanSlotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanSlotStatus): Boolean {
        when (status) {
            LanePlanSlotStatus.DRAFT -> {
                when (next) {
                    LanePlanSlotStatus.SUSPENDED -> return true
                }
                return false
            }
            LanePlanSlotStatus.PENDING -> {
                when (next) {
                    LanePlanSlotStatus.SUSPENDED -> return true
                }
                return false
            }
            LanePlanSlotStatus.ACTIVE -> {
                when (next) {
                    LanePlanSlotStatus.SUSPENDED, LanePlanSlotStatus.CLOSED -> return true
                }
                return false
            }
            LanePlanSlotStatus.SUSPENDED -> {
                when (next) {
                    LanePlanSlotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanSlotStatus) {
        if (!canMoveTo(next)) {
            error("lane plan slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = discount

    fun snapshot(): LanePlanSlot {
        return LanePlanSlot(
            code = code,
            name = name,
            reviewer = reviewer,
            locale = locale,
            discount = discount,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lane plan slot in status draft. */
fun newLanePlanSlot(code: String, name: String): LanePlanSlot {
    return LanePlanSlot(
        code = code,
        name = name,
        reviewer = "",
        locale = "",
        discount = 0.0,
        ownerId = "",
        status = LanePlanSlotStatus.DRAFT,
    )
}
