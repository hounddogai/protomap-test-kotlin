package com.acme.transport.terminal.dockdoor.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door notice is in its life cycle. */
enum class DockDoorNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A dock door notice as the dock door team keeps it. */
data class DockDoorNotice(
    var code: String,
    var label: String,
    var ratio: Double,
    var locale: String,
    var retries: Int,
    var contactEmail: String,
    var status: DockDoorNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorNoticeStatus): Boolean {
        when (status) {
            DockDoorNoticeStatus.PROPOSED -> return next == DockDoorNoticeStatus.APPROVED || next == DockDoorNoticeStatus.WITHDRAWN
            DockDoorNoticeStatus.APPROVED -> return next == DockDoorNoticeStatus.REJECTED || next == DockDoorNoticeStatus.WITHDRAWN
            DockDoorNoticeStatus.REJECTED -> return next == DockDoorNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DockDoorNoticeStatus) {
        if (!canMoveTo(next)) {
            error("dock door notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round(ratio * retries.toDouble() * 100) / 100

    fun snapshot(): DockDoorNotice {
        return DockDoorNotice(
            code = code,
            label = label,
            ratio = ratio,
            locale = locale,
            retries = retries,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a dock door notice in status proposed. */
fun newDockDoorNotice(code: String, label: String): DockDoorNotice {
    return DockDoorNotice(
        code = code,
        label = label,
        ratio = 0.0,
        locale = "",
        retries = 0,
        contactEmail = "",
        status = DockDoorNoticeStatus.PROPOSED,
    )
}
