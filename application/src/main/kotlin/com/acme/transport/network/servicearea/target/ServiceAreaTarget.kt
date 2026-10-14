package com.acme.transport.network.servicearea.target

import com.acme.transport.kit.text.normalize

// The service area target records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area target is in its life cycle. */
enum class ServiceAreaTargetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a service area target and its current status. */
data class ServiceAreaTarget(
    var code: String,
    var title: String,
    var currency: String,
    var lane: String,
    var priority: Int,
    var status: ServiceAreaTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaTargetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaTargetStatus): Boolean {
        when (status) {
            ServiceAreaTargetStatus.IDLE -> return next == ServiceAreaTargetStatus.STOPPED
            ServiceAreaTargetStatus.WARMING -> return next == ServiceAreaTargetStatus.READY || next == ServiceAreaTargetStatus.DRAINING
            ServiceAreaTargetStatus.READY -> return next == ServiceAreaTargetStatus.DRAINING || next == ServiceAreaTargetStatus.STOPPED
            ServiceAreaTargetStatus.DRAINING -> return next == ServiceAreaTargetStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServiceAreaTargetStatus) {
        if (!canMoveTo(next)) {
            error("service area target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"service area target"} $code"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): ServiceAreaTarget {
        return ServiceAreaTarget(
            code = code,
            title = title,
            currency = currency,
            lane = lane,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service area target in status idle. */
fun newServiceAreaTarget(code: String, title: String): ServiceAreaTarget {
    return ServiceAreaTarget(
        code = code,
        title = title,
        currency = "",
        lane = "",
        priority = 0,
        status = ServiceAreaTargetStatus.IDLE,
    )
}
