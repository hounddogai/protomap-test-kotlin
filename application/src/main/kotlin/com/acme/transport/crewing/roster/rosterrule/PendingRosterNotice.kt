package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.kit.text.normalize

/** Where a pending roster notice is in its life cycle. */
enum class PendingRosterNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A pending roster notice. Its code is unique within a tenant. */
data class PendingRosterNotice(
    var code: String,
    var name: String,
    var channel: String,
    var archived: Boolean,
    var driverId: String,
    var score: Double,
    var status: PendingRosterNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRosterNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRosterNoticeStatus): Boolean {
        when (status) {
            PendingRosterNoticeStatus.PLANNED -> {
                when (next) {
                    PendingRosterNoticeStatus.IN_PROGRESS, PendingRosterNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            PendingRosterNoticeStatus.SCHEDULED -> return next == PendingRosterNoticeStatus.IN_PROGRESS || next == PendingRosterNoticeStatus.CANCELLED
            PendingRosterNoticeStatus.IN_PROGRESS -> return next == PendingRosterNoticeStatus.COMPLETED
            PendingRosterNoticeStatus.COMPLETED -> return next == PendingRosterNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingRosterNoticeStatus) {
        if (!canMoveTo(next)) {
            error("pending roster notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = score

    fun snapshot(): PendingRosterNotice {
        return PendingRosterNotice(
            code = code,
            name = name,
            channel = channel,
            archived = archived,
            driverId = driverId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending roster notice in status planned. */
fun newPendingRosterNotice(code: String, name: String): PendingRosterNotice {
    return PendingRosterNotice(
        code = code,
        name = name,
        channel = "",
        archived = false,
        driverId = "",
        score = 0.0,
        status = PendingRosterNoticeStatus.PLANNED,
    )
}
