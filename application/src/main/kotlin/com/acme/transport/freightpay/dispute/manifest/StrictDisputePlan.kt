package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dispute plan is in its life cycle. */
enum class StrictDisputePlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a strict dispute plan and its current status. */
data class StrictDisputePlan(
    var code: String,
    var title: String,
    var checksum: String,
    var lane: String,
    var graceDays: Int,
    var retries: Int,
    var channel: String,
    var startsAt: Int,
    var contactEmail: String,
    var status: StrictDisputePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDisputePlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDisputePlanStatus): Boolean {
        when (status) {
            StrictDisputePlanStatus.PROPOSED -> return next == StrictDisputePlanStatus.APPROVED || next == StrictDisputePlanStatus.WITHDRAWN
            StrictDisputePlanStatus.APPROVED -> return next == StrictDisputePlanStatus.REJECTED || next == StrictDisputePlanStatus.WITHDRAWN
            StrictDisputePlanStatus.REJECTED -> return next == StrictDisputePlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictDisputePlanStatus) {
        if (!canMoveTo(next)) {
            error("strict dispute plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((retries.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictDisputePlan {
        return StrictDisputePlan(
            code = code,
            title = title,
            checksum = checksum,
            lane = lane,
            graceDays = graceDays,
            retries = retries,
            channel = channel,
            startsAt = startsAt,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict dispute plan in status proposed. */
fun newStrictDisputePlan(code: String, title: String): StrictDisputePlan {
    return StrictDisputePlan(
        code = code,
        title = title,
        checksum = "",
        lane = "",
        graceDays = 0,
        retries = 0,
        channel = "",
        startsAt = 0,
        contactEmail = "",
        status = StrictDisputePlanStatus.PROPOSED,
    )
}
