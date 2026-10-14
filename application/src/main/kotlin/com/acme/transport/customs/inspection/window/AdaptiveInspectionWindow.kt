package com.acme.transport.customs.inspection.window

import com.acme.transport.kit.text.normalize

// The adaptive inspection window records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a adaptive inspection window is in its life cycle. */
enum class AdaptiveInspectionWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive inspection window, with what the planners need to act on it. */
data class AdaptiveInspectionWindow(
    var code: String,
    var label: String,
    var ownerId: String,
    var etaMinutes: Int,
    var description: String,
    var checksum: String,
    var depot: String,
    var status: AdaptiveInspectionWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionWindowStatus): Boolean {
        when (status) {
            AdaptiveInspectionWindowStatus.DRAFT -> return next == AdaptiveInspectionWindowStatus.PENDING || next == AdaptiveInspectionWindowStatus.ACTIVE
            AdaptiveInspectionWindowStatus.PENDING -> {
                when (next) {
                    AdaptiveInspectionWindowStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveInspectionWindowStatus.ACTIVE -> return next == AdaptiveInspectionWindowStatus.SUSPENDED
            AdaptiveInspectionWindowStatus.SUSPENDED -> return next == AdaptiveInspectionWindowStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive inspection window"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): AdaptiveInspectionWindow {
        return AdaptiveInspectionWindow(
            code = code,
            label = label,
            ownerId = ownerId,
            etaMinutes = etaMinutes,
            description = description,
            checksum = checksum,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive inspection window in status draft. */
fun newAdaptiveInspectionWindow(code: String, label: String): AdaptiveInspectionWindow {
    return AdaptiveInspectionWindow(
        code = code,
        label = label,
        ownerId = "",
        etaMinutes = 0,
        description = "",
        checksum = "",
        depot = "",
        status = AdaptiveInspectionWindowStatus.DRAFT,
    )
}
