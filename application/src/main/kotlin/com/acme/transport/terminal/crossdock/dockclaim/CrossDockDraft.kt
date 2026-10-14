package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock draft is in its life cycle. */
enum class CrossDockDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A cross dock draft as the cross dock team keeps it. */
data class CrossDockDraft(
    var code: String,
    var title: String,
    var capacity: Int,
    var phone: String,
    var attempts: Int,
    var odometer: Int,
    var threshold: Double,
    var endsAt: Int,
    var status: CrossDockDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockDraftStatus): Boolean {
        when (status) {
            CrossDockDraftStatus.IDLE -> return next == CrossDockDraftStatus.READY || next == CrossDockDraftStatus.DRAINING
            CrossDockDraftStatus.WARMING -> return next == CrossDockDraftStatus.READY || next == CrossDockDraftStatus.DRAINING
            CrossDockDraftStatus.READY -> return next == CrossDockDraftStatus.DRAINING || next == CrossDockDraftStatus.STOPPED
            CrossDockDraftStatus.DRAINING -> return next == CrossDockDraftStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CrossDockDraftStatus) {
        if (!canMoveTo(next)) {
            error("cross dock draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"cross dock draft"} $code"

    fun adjustedTotal(): Double = round((capacity.toDouble() + attempts.toDouble()) * 100) / 100

    fun snapshot(): CrossDockDraft {
        return CrossDockDraft(
            code = code,
            title = title,
            capacity = capacity,
            phone = phone,
            attempts = attempts,
            odometer = odometer,
            threshold = threshold,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock draft in status idle. */
fun newCrossDockDraft(code: String, title: String): CrossDockDraft {
    return CrossDockDraft(
        code = code,
        title = title,
        capacity = 0,
        phone = "",
        attempts = 0,
        odometer = 0,
        threshold = 0.0,
        endsAt = 0,
        status = CrossDockDraftStatus.IDLE,
    )
}
