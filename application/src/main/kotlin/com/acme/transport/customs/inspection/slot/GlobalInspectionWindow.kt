package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.text.normalize

/** Where a global inspection window is in its life cycle. */
enum class GlobalInspectionWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a global inspection window and its current status. */
data class GlobalInspectionWindow(
    var code: String,
    var title: String,
    var ownerId: String,
    var version: Int,
    var shiftCode: String,
    var status: GlobalInspectionWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionWindowStatus): Boolean {
        when (status) {
            GlobalInspectionWindowStatus.DRAFT -> {
                when (next) {
                    GlobalInspectionWindowStatus.ACTIVE -> return true
                }
                return false
            }
            GlobalInspectionWindowStatus.PENDING -> {
                when (next) {
                    GlobalInspectionWindowStatus.ACTIVE, GlobalInspectionWindowStatus.CLOSED -> return true
                }
                return false
            }
            GlobalInspectionWindowStatus.ACTIVE -> return next == GlobalInspectionWindowStatus.CLOSED
            GlobalInspectionWindowStatus.SUSPENDED -> {
                when (next) {
                    GlobalInspectionWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalInspectionWindowStatus) {
        if (!canMoveTo(next)) {
            error("global inspection window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): GlobalInspectionWindow {
        return GlobalInspectionWindow(
            code = code,
            title = title,
            ownerId = ownerId,
            version = version,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global inspection window in status draft. */
fun newGlobalInspectionWindow(code: String, title: String): GlobalInspectionWindow {
    return GlobalInspectionWindow(
        code = code,
        title = title,
        ownerId = "",
        version = 0,
        shiftCode = "",
        status = GlobalInspectionWindowStatus.DRAFT,
    )
}
