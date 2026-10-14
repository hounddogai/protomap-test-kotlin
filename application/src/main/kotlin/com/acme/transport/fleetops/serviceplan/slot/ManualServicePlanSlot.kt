package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.kit.text.normalize

// The manual service plan slot records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a manual service plan slot is in its life cycle. */
enum class ManualServicePlanSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual service plan slot, with what the planners need to act on it. */
data class ManualServicePlanSlot(
    var code: String,
    var label: String,
    var dock: String,
    var notes: String,
    var lane: String,
    var score: Double,
    var status: ManualServicePlanSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualServicePlanSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualServicePlanSlotStatus): Boolean {
        when (status) {
            ManualServicePlanSlotStatus.OPEN -> {
                when (next) {
                    ManualServicePlanSlotStatus.IN_REVIEW -> return true
                }
                return false
            }
            ManualServicePlanSlotStatus.IN_REVIEW -> return next == ManualServicePlanSlotStatus.REOPENED || next == ManualServicePlanSlotStatus.ARCHIVED
            ManualServicePlanSlotStatus.RESOLVED -> {
                when (next) {
                    ManualServicePlanSlotStatus.REOPENED, ManualServicePlanSlotStatus.ARCHIVED -> return true
                }
                return false
            }
            ManualServicePlanSlotStatus.REOPENED -> {
                when (next) {
                    ManualServicePlanSlotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualServicePlanSlotStatus) {
        if (!canMoveTo(next)) {
            error("manual service plan slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): ManualServicePlanSlot {
        return ManualServicePlanSlot(
            code = code,
            label = label,
            dock = dock,
            notes = notes,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual service plan slot in status open. */
fun newManualServicePlanSlot(code: String, label: String): ManualServicePlanSlot {
    return ManualServicePlanSlot(
        code = code,
        label = label,
        dock = "",
        notes = "",
        lane = "",
        score = 0.0,
        status = ManualServicePlanSlotStatus.OPEN,
    )
}
