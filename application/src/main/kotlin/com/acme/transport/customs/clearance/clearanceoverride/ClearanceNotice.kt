package com.acme.transport.customs.clearance.clearanceoverride

import com.acme.transport.kit.text.normalize

/** Where a clearance notice is in its life cycle. */
enum class ClearanceNoticeStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One clearance notice, with what the planners need to act on it. */
data class ClearanceNotice(
    var code: String,
    var label: String,
    var dock: String,
    var stopCount: Int,
    var routeCode: String,
    var status: ClearanceNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceNoticeStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceNoticeStatus): Boolean {
        when (status) {
            ClearanceNoticeStatus.CREATED -> {
                when (next) {
                    ClearanceNoticeStatus.RETIRED -> return true
                }
                return false
            }
            ClearanceNoticeStatus.VALIDATED -> {
                when (next) {
                    ClearanceNoticeStatus.RETIRED -> return true
                }
                return false
            }
            ClearanceNoticeStatus.PUBLISHED -> return next == ClearanceNoticeStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ClearanceNoticeStatus) {
        if (!canMoveTo(next)) {
            error("clearance notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"clearance notice"} $code"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): ClearanceNotice {
        return ClearanceNotice(
            code = code,
            label = label,
            dock = dock,
            stopCount = stopCount,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a clearance notice in status created. */
fun newClearanceNotice(code: String, label: String): ClearanceNotice {
    return ClearanceNotice(
        code = code,
        label = label,
        dock = "",
        stopCount = 0,
        routeCode = "",
        status = ClearanceNoticeStatus.CREATED,
    )
}
