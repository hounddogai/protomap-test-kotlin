package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset request is in its life cycle. */
enum class RouteOffsetRequestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A route offset request as the route offset team keeps it. */
data class RouteOffsetRequest(
    var code: String,
    var title: String,
    var retries: Int,
    var active: Boolean,
    var createdAt: Int,
    var channel: String,
    var pinned: Boolean,
    var lane: String,
    var stopCount: Int,
    var pallets: Int,
    var attempts: Int,
    var status: RouteOffsetRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetRequestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetRequestStatus): Boolean {
        when (status) {
            RouteOffsetRequestStatus.NEW -> {
                when (next) {
                    RouteOffsetRequestStatus.DONE, RouteOffsetRequestStatus.FAILED -> return true
                }
                return false
            }
            RouteOffsetRequestStatus.QUEUED -> {
                when (next) {
                    RouteOffsetRequestStatus.DONE -> return true
                }
                return false
            }
            RouteOffsetRequestStatus.RUNNING -> {
                when (next) {
                    RouteOffsetRequestStatus.FAILED -> return true
                }
                return false
            }
            RouteOffsetRequestStatus.DONE -> {
                when (next) {
                    RouteOffsetRequestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetRequestStatus) {
        if (!canMoveTo(next)) {
            error("route offset request $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"route offset request"} $code"

    fun weightedVolume(): Double = round(stopCount.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetRequest {
        return RouteOffsetRequest(
            code = code,
            title = title,
            retries = retries,
            active = active,
            createdAt = createdAt,
            channel = channel,
            pinned = pinned,
            lane = lane,
            stopCount = stopCount,
            pallets = pallets,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a route offset request in status new. */
fun newRouteOffsetRequest(code: String, title: String): RouteOffsetRequest {
    return RouteOffsetRequest(
        code = code,
        title = title,
        retries = 0,
        active = false,
        createdAt = 0,
        channel = "",
        pinned = false,
        lane = "",
        stopCount = 0,
        pallets = 0,
        attempts = 0,
        status = RouteOffsetRequestStatus.NEW,
    )
}
