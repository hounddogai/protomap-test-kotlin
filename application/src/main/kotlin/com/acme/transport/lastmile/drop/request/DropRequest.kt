package com.acme.transport.lastmile.drop.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The drop request records of lastmile's drop work, and the rules, stores, and calculations around them.

/** Where a drop request is in its life cycle. */
enum class DropRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One drop request, with what the planners need to act on it. */
data class DropRequest(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var archived: Boolean,
    var discount: Double,
    var lane: String,
    var ceiling: Double,
    var carrier: String,
    var minimum: Double,
    var ownerId: String,
    var batchSize: Int,
    var status: DropRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropRequestStatus): Boolean {
        when (status) {
            DropRequestStatus.DRAFT -> {
                when (next) {
                    DropRequestStatus.ACTIVE -> return true
                }
                return false
            }
            DropRequestStatus.PENDING -> return next == DropRequestStatus.SUSPENDED || next == DropRequestStatus.CLOSED
            DropRequestStatus.ACTIVE -> return next == DropRequestStatus.SUSPENDED
            DropRequestStatus.SUSPENDED -> return next == DropRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DropRequestStatus) {
        if (!canMoveTo(next)) {
            error("drop request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveExposure(): Double = round((discount - fuelLiters) * 100) / 100

    fun snapshot(): DropRequest {
        return DropRequest(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            archived = archived,
            discount = discount,
            lane = lane,
            ceiling = ceiling,
            carrier = carrier,
            minimum = minimum,
            ownerId = ownerId,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a drop request in status draft. */
fun newDropRequest(code: String, name: String): DropRequest {
    return DropRequest(
        code = code,
        name = name,
        fuelLiters = 0.0,
        archived = false,
        discount = 0.0,
        lane = "",
        ceiling = 0.0,
        carrier = "",
        minimum = 0.0,
        ownerId = "",
        batchSize = 0,
        status = DropRequestStatus.DRAFT,
    )
}
