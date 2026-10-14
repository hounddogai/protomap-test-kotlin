package com.acme.transport.linehaul.tender.tenderestimate

import com.acme.transport.kit.text.normalize

// The strict tender estimate records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a strict tender estimate is in its life cycle. */
enum class StrictTenderEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a strict tender estimate and its current status. */
data class StrictTenderEstimate(
    var code: String,
    var title: String,
    var description: String,
    var label: String,
    var pinned: Boolean,
    var windowDays: Int,
    var status: StrictTenderEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTenderEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTenderEstimateStatus): Boolean {
        when (status) {
            StrictTenderEstimateStatus.IDLE -> {
                when (next) {
                    StrictTenderEstimateStatus.WARMING -> return true
                }
                return false
            }
            StrictTenderEstimateStatus.WARMING -> {
                when (next) {
                    StrictTenderEstimateStatus.DRAINING -> return true
                }
                return false
            }
            StrictTenderEstimateStatus.READY -> return next == StrictTenderEstimateStatus.DRAINING || next == StrictTenderEstimateStatus.STOPPED
            StrictTenderEstimateStatus.DRAINING -> return next == StrictTenderEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictTenderEstimateStatus) {
        if (!canMoveTo(next)) {
            error("strict tender estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict tender estimate"} $code"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): StrictTenderEstimate {
        return StrictTenderEstimate(
            code = code,
            title = title,
            description = description,
            label = label,
            pinned = pinned,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict tender estimate in status idle. */
fun newStrictTenderEstimate(code: String, title: String): StrictTenderEstimate {
    return StrictTenderEstimate(
        code = code,
        title = title,
        description = "",
        label = "",
        pinned = false,
        windowDays = 0,
        status = StrictTenderEstimateStatus.IDLE,
    )
}
