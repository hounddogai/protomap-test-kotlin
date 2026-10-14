package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled broker request records of customs's broker work, and the rules, stores, and calculations
// around them.

/** Where a scheduled broker request is in its life cycle. */
enum class ScheduledBrokerRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled broker request and its current status. */
data class ScheduledBrokerRequest(
    var code: String,
    var name: String,
    var floorValue: Double,
    var title: String,
    var distanceKm: Double,
    var status: ScheduledBrokerRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledBrokerRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledBrokerRequestStatus): Boolean {
        when (status) {
            ScheduledBrokerRequestStatus.OPEN -> {
                when (next) {
                    ScheduledBrokerRequestStatus.RESOLVED, ScheduledBrokerRequestStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledBrokerRequestStatus.IN_REVIEW -> return next == ScheduledBrokerRequestStatus.RESOLVED
            ScheduledBrokerRequestStatus.RESOLVED -> return next == ScheduledBrokerRequestStatus.REOPENED || next == ScheduledBrokerRequestStatus.ARCHIVED
            ScheduledBrokerRequestStatus.REOPENED -> {
                when (next) {
                    ScheduledBrokerRequestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledBrokerRequestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled broker request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled broker request"} $code"

    fun netTotal(): Double = round((distanceKm - floorValue) * 100) / 100

    fun snapshot(): ScheduledBrokerRequest {
        return ScheduledBrokerRequest(
            code = code,
            name = name,
            floorValue = floorValue,
            title = title,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled broker request in status open. */
fun newScheduledBrokerRequest(code: String, name: String): ScheduledBrokerRequest {
    return ScheduledBrokerRequest(
        code = code,
        name = name,
        floorValue = 0.0,
        title = "",
        distanceKm = 0.0,
        status = ScheduledBrokerRequestStatus.OPEN,
    )
}
