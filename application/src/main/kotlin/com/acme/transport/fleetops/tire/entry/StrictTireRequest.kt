package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict tire request is in its life cycle. */
enum class StrictTireRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict tire request as the tire team keeps it. */
data class StrictTireRequest(
    var code: String,
    var title: String,
    var ceiling: Double,
    var source: String,
    var country: String,
    var amount: Double,
    var routeCode: String,
    var priority: Int,
    var startsAt: Int,
    var checksum: String,
    var vehicleId: String,
    var status: StrictTireRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTireRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTireRequestStatus): Boolean {
        when (status) {
            StrictTireRequestStatus.NEW -> {
                when (next) {
                    StrictTireRequestStatus.RUNNING, StrictTireRequestStatus.FAILED -> return true
                }
                return false
            }
            StrictTireRequestStatus.QUEUED -> return next == StrictTireRequestStatus.DONE
            StrictTireRequestStatus.RUNNING -> return next == StrictTireRequestStatus.DONE || next == StrictTireRequestStatus.FAILED
            StrictTireRequestStatus.DONE -> {
                when (next) {
                    StrictTireRequestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTireRequestStatus) {
        if (!canMoveTo(next)) {
            error("strict tire request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round(priority.toDouble() * amount * 100) / 100

    fun snapshot(): StrictTireRequest {
        return StrictTireRequest(
            code = code,
            title = title,
            ceiling = ceiling,
            source = source,
            country = country,
            amount = amount,
            routeCode = routeCode,
            priority = priority,
            startsAt = startsAt,
            checksum = checksum,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a strict tire request in status new. */
fun newStrictTireRequest(code: String, title: String): StrictTireRequest {
    return StrictTireRequest(
        code = code,
        title = title,
        ceiling = 0.0,
        source = "",
        country = "",
        amount = 0.0,
        routeCode = "",
        priority = 0,
        startsAt = 0,
        checksum = "",
        vehicleId = "",
        status = StrictTireRequestStatus.NEW,
    )
}
