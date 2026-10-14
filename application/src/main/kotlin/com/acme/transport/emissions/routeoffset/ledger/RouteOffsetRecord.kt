package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset record is in its life cycle. */
enum class RouteOffsetRecordStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A route offset record. Its code is unique within a tenant. */
data class RouteOffsetRecord(
    var code: String,
    var name: String,
    var batchSize: Int,
    var ceiling: Double,
    var visible: Boolean,
    var status: RouteOffsetRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetRecordStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetRecordStatus): Boolean {
        when (status) {
            RouteOffsetRecordStatus.IDLE -> return next == RouteOffsetRecordStatus.READY || next == RouteOffsetRecordStatus.STOPPED
            RouteOffsetRecordStatus.WARMING -> return next == RouteOffsetRecordStatus.DRAINING || next == RouteOffsetRecordStatus.STOPPED
            RouteOffsetRecordStatus.READY -> return next == RouteOffsetRecordStatus.STOPPED
            RouteOffsetRecordStatus.DRAINING -> {
                when (next) {
                    RouteOffsetRecordStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetRecordStatus) {
        if (!canMoveTo(next)) {
            error("route offset record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"route offset record"} $code"

    fun weightedLoad(): Double = round(batchSize.toDouble() * ceiling * 100) / 100

    fun snapshot(): RouteOffsetRecord {
        return RouteOffsetRecord(
            code = code,
            name = name,
            batchSize = batchSize,
            ceiling = ceiling,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a route offset record in status idle. */
fun newRouteOffsetRecord(code: String, name: String): RouteOffsetRecord {
    return RouteOffsetRecord(
        code = code,
        name = name,
        batchSize = 0,
        ceiling = 0.0,
        visible = false,
        status = RouteOffsetRecordStatus.IDLE,
    )
}
