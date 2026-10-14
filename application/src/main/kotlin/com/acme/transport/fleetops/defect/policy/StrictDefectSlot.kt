package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.text.normalize

/** Where a strict defect slot is in its life cycle. */
enum class StrictDefectSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict defect slot as the defect team keeps it. */
data class StrictDefectSlot(
    var code: String,
    var name: String,
    var stopCount: Int,
    var approved: Boolean,
    var locale: String,
    var summary: String,
    var lane: String,
    var country: String,
    var pinned: Boolean,
    var status: StrictDefectSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDefectSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDefectSlotStatus): Boolean {
        when (status) {
            StrictDefectSlotStatus.NEW -> {
                when (next) {
                    StrictDefectSlotStatus.QUEUED, StrictDefectSlotStatus.RUNNING -> return true
                }
                return false
            }
            StrictDefectSlotStatus.QUEUED -> {
                when (next) {
                    StrictDefectSlotStatus.DONE, StrictDefectSlotStatus.FAILED -> return true
                }
                return false
            }
            StrictDefectSlotStatus.RUNNING -> {
                when (next) {
                    StrictDefectSlotStatus.DONE, StrictDefectSlotStatus.FAILED -> return true
                }
                return false
            }
            StrictDefectSlotStatus.DONE -> {
                when (next) {
                    StrictDefectSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDefectSlotStatus) {
        if (!canMoveTo(next)) {
            error("strict defect slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): StrictDefectSlot {
        return StrictDefectSlot(
            code = code,
            name = name,
            stopCount = stopCount,
            approved = approved,
            locale = locale,
            summary = summary,
            lane = lane,
            country = country,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict defect slot in status new. */
fun newStrictDefectSlot(code: String, name: String): StrictDefectSlot {
    return StrictDefectSlot(
        code = code,
        name = name,
        stopCount = 0,
        approved = false,
        locale = "",
        summary = "",
        lane = "",
        country = "",
        pinned = false,
        status = StrictDefectSlotStatus.NEW,
    )
}
