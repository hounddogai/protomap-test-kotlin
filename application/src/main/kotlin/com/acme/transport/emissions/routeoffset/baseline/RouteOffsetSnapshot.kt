package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset snapshot is in its life cycle. */
enum class RouteOffsetSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a route offset snapshot and its current status. */
data class RouteOffsetSnapshot(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var limit: Int,
    var etaMinutes: Int,
    var trailerId: String,
    var notes: String,
    var status: RouteOffsetSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetSnapshotStatus): Boolean {
        when (status) {
            RouteOffsetSnapshotStatus.IDLE -> {
                when (next) {
                    RouteOffsetSnapshotStatus.READY -> return true
                }
                return false
            }
            RouteOffsetSnapshotStatus.WARMING -> {
                when (next) {
                    RouteOffsetSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            RouteOffsetSnapshotStatus.READY -> {
                when (next) {
                    RouteOffsetSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            RouteOffsetSnapshotStatus.DRAINING -> {
                when (next) {
                    RouteOffsetSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("route offset snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round(etaMinutes.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetSnapshot {
        return RouteOffsetSnapshot(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            limit = limit,
            etaMinutes = etaMinutes,
            trailerId = trailerId,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a route offset snapshot in status idle. */
fun newRouteOffsetSnapshot(code: String, name: String): RouteOffsetSnapshot {
    return RouteOffsetSnapshot(
        code = code,
        name = name,
        durationMinutes = 0,
        limit = 0,
        etaMinutes = 0,
        trailerId = "",
        notes = "",
        status = RouteOffsetSnapshotStatus.IDLE,
    )
}
