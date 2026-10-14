package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.kit.text.normalize

/** Where a scheduled accessorial notice is in its life cycle. */
enum class ScheduledAccessorialNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled accessorial notice and its current status. */
data class ScheduledAccessorialNotice(
    var code: String,
    var name: String,
    var channel: String,
    var approved: Boolean,
    var source: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var status: ScheduledAccessorialNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAccessorialNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAccessorialNoticeStatus): Boolean {
        when (status) {
            ScheduledAccessorialNoticeStatus.NEW -> return next == ScheduledAccessorialNoticeStatus.RUNNING || next == ScheduledAccessorialNoticeStatus.DONE
            ScheduledAccessorialNoticeStatus.QUEUED -> return next == ScheduledAccessorialNoticeStatus.RUNNING
            ScheduledAccessorialNoticeStatus.RUNNING -> return next == ScheduledAccessorialNoticeStatus.DONE || next == ScheduledAccessorialNoticeStatus.FAILED
            ScheduledAccessorialNoticeStatus.DONE -> {
                when (next) {
                    ScheduledAccessorialNoticeStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledAccessorialNoticeStatus) {
        if (!canMoveTo(next)) {
            error("scheduled accessorial notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled accessorial notice"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): ScheduledAccessorialNotice {
        return ScheduledAccessorialNotice(
            code = code,
            name = name,
            channel = channel,
            approved = approved,
            source = source,
            sequence = sequence,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled accessorial notice in status new. */
fun newScheduledAccessorialNotice(code: String, name: String): ScheduledAccessorialNotice {
    return ScheduledAccessorialNotice(
        code = code,
        name = name,
        channel = "",
        approved = false,
        source = "",
        sequence = 0,
        tags = mutableListOf<String>(),
        status = ScheduledAccessorialNoticeStatus.NEW,
    )
}
