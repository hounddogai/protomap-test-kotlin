package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled route offset checkpoint records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a scheduled route offset checkpoint is in its life cycle. */
enum class ScheduledRouteOffsetCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scheduled route offset checkpoint as the route offset team keeps it. */
data class ScheduledRouteOffsetCheckpoint(
    var code: String,
    var name: String,
    var endsAt: Int,
    var limit: Int,
    var pallets: Int,
    var batchSize: Int,
    var externalRef: String,
    var quantity: Int,
    var carrier: String,
    var depot: String,
    var status: ScheduledRouteOffsetCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRouteOffsetCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRouteOffsetCheckpointStatus): Boolean {
        when (status) {
            ScheduledRouteOffsetCheckpointStatus.REQUESTED -> {
                when (next) {
                    ScheduledRouteOffsetCheckpointStatus.REVOKED, ScheduledRouteOffsetCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            ScheduledRouteOffsetCheckpointStatus.GRANTED -> return next == ScheduledRouteOffsetCheckpointStatus.REVOKED
            ScheduledRouteOffsetCheckpointStatus.REVOKED -> return next == ScheduledRouteOffsetCheckpointStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScheduledRouteOffsetCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("scheduled route offset checkpoint $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((quantity.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRouteOffsetCheckpoint {
        return ScheduledRouteOffsetCheckpoint(
            code = code,
            name = name,
            endsAt = endsAt,
            limit = limit,
            pallets = pallets,
            batchSize = batchSize,
            externalRef = externalRef,
            quantity = quantity,
            carrier = carrier,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled route offset checkpoint in status requested. */
fun newScheduledRouteOffsetCheckpoint(code: String, name: String): ScheduledRouteOffsetCheckpoint {
    return ScheduledRouteOffsetCheckpoint(
        code = code,
        name = name,
        endsAt = 0,
        limit = 0,
        pallets = 0,
        batchSize = 0,
        externalRef = "",
        quantity = 0,
        carrier = "",
        depot = "",
        status = ScheduledRouteOffsetCheckpointStatus.REQUESTED,
    )
}
