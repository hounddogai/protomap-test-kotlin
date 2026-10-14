package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.text.normalize

/** Where a lenient inspection window is in its life cycle. */
enum class LenientInspectionWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lenient inspection window and its current status. */
data class LenientInspectionWindow(
    var code: String,
    var label: String,
    var region: String,
    var updatedAt: Int,
    var archived: Boolean,
    var status: LenientInspectionWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientInspectionWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientInspectionWindowStatus): Boolean {
        when (status) {
            LenientInspectionWindowStatus.NEW -> return next == LenientInspectionWindowStatus.FAILED
            LenientInspectionWindowStatus.QUEUED -> return next == LenientInspectionWindowStatus.DONE || next == LenientInspectionWindowStatus.FAILED
            LenientInspectionWindowStatus.RUNNING -> return next == LenientInspectionWindowStatus.DONE
            LenientInspectionWindowStatus.DONE -> {
                when (next) {
                    LenientInspectionWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientInspectionWindowStatus) {
        if (!canMoveTo(next)) {
            error("lenient inspection window $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = updatedAt.toDouble()

    fun snapshot(): LenientInspectionWindow {
        return LenientInspectionWindow(
            code = code,
            label = label,
            region = region,
            updatedAt = updatedAt,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient inspection window in status new. */
fun newLenientInspectionWindow(code: String, label: String): LenientInspectionWindow {
    return LenientInspectionWindow(
        code = code,
        label = label,
        region = "",
        updatedAt = 0,
        archived = false,
        status = LenientInspectionWindowStatus.NEW,
    )
}
