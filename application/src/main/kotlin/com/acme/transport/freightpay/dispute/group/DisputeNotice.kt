package com.acme.transport.freightpay.dispute.group

import com.acme.transport.kit.text.normalize

/** Where a dispute notice is in its life cycle. */
enum class DisputeNoticeStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a dispute notice and its current status. */
data class DisputeNotice(
    var code: String,
    var name: String,
    var description: String,
    var weight: Double,
    var ownerId: String,
    var status: DisputeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeNoticeStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeNoticeStatus): Boolean {
        when (status) {
            DisputeNoticeStatus.NEW -> {
                when (next) {
                    DisputeNoticeStatus.QUEUED, DisputeNoticeStatus.DONE -> return true
                }
                return false
            }
            DisputeNoticeStatus.QUEUED -> return next == DisputeNoticeStatus.RUNNING
            DisputeNoticeStatus.RUNNING -> return next == DisputeNoticeStatus.DONE || next == DisputeNoticeStatus.FAILED
            DisputeNoticeStatus.DONE -> return next == DisputeNoticeStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DisputeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("dispute notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = weight

    fun snapshot(): DisputeNotice {
        return DisputeNotice(
            code = code,
            name = name,
            description = description,
            weight = weight,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dispute notice in status new. */
fun newDisputeNotice(code: String, name: String): DisputeNotice {
    return DisputeNotice(
        code = code,
        name = name,
        description = "",
        weight = 0.0,
        ownerId = "",
        status = DisputeNoticeStatus.NEW,
    )
}
