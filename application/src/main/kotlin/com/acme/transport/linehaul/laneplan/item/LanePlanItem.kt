package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.normalize

// The lane plan item records of linehaul's lane plan work, and the rules, stores, and calculations around
// them.

/** Where a lane plan item is in its life cycle. */
enum class LanePlanItemStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lane plan item and its current status. */
data class LanePlanItem(
    var code: String,
    var label: String,
    var notes: String,
    var segment: String,
    var etaMinutes: Int,
    var lane: String,
    var status: LanePlanItemStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanItemStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanItemStatus): Boolean {
        when (status) {
            LanePlanItemStatus.PLANNED -> {
                when (next) {
                    LanePlanItemStatus.SCHEDULED -> return true
                }
                return false
            }
            LanePlanItemStatus.SCHEDULED -> {
                when (next) {
                    LanePlanItemStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LanePlanItemStatus.IN_PROGRESS -> return next == LanePlanItemStatus.COMPLETED
            LanePlanItemStatus.COMPLETED -> {
                when (next) {
                    LanePlanItemStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanItemStatus) {
        if (!canMoveTo(next)) {
            error("lane plan item $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): LanePlanItem {
        return LanePlanItem(
            code = code,
            label = label,
            notes = notes,
            segment = segment,
            etaMinutes = etaMinutes,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lane plan item in status planned. */
fun newLanePlanItem(code: String, label: String): LanePlanItem {
    return LanePlanItem(
        code = code,
        label = label,
        notes = "",
        segment = "",
        etaMinutes = 0,
        lane = "",
        status = LanePlanItemStatus.PLANNED,
    )
}
