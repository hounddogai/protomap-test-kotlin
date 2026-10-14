package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute plan is in its life cycle. */
enum class DisputePlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A dispute plan. Its code is unique within a tenant. */
data class DisputePlan(
    var code: String,
    var label: String,
    var depot: String,
    var retries: Int,
    var version: Int,
    var name: String,
    var lane: String,
    var graceDays: Int,
    var timezone: String,
    var cost: Double,
    var status: DisputePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputePlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputePlanStatus): Boolean {
        when (status) {
            DisputePlanStatus.IDLE -> return next == DisputePlanStatus.WARMING || next == DisputePlanStatus.STOPPED
            DisputePlanStatus.WARMING -> {
                when (next) {
                    DisputePlanStatus.READY, DisputePlanStatus.STOPPED -> return true
                }
                return false
            }
            DisputePlanStatus.READY -> {
                when (next) {
                    DisputePlanStatus.DRAINING, DisputePlanStatus.STOPPED -> return true
                }
                return false
            }
            DisputePlanStatus.DRAINING -> {
                when (next) {
                    DisputePlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputePlanStatus) {
        if (!canMoveTo(next)) {
            error("dispute plan $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((retries.toDouble() + cost) * 100) / 100

    fun snapshot(): DisputePlan {
        return DisputePlan(
            code = code,
            label = label,
            depot = depot,
            retries = retries,
            version = version,
            name = name,
            lane = lane,
            graceDays = graceDays,
            timezone = timezone,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dispute plan in status idle. */
fun newDisputePlan(code: String, label: String): DisputePlan {
    return DisputePlan(
        code = code,
        label = label,
        depot = "",
        retries = 0,
        version = 0,
        name = "",
        lane = "",
        graceDays = 0,
        timezone = "",
        cost = 0.0,
        status = DisputePlanStatus.IDLE,
    )
}
