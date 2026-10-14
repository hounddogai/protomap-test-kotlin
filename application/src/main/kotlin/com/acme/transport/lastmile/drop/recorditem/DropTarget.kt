package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop target is in its life cycle. */
enum class DropTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One drop target, with what the planners need to act on it. */
data class DropTarget(
    var code: String,
    var title: String,
    var stopCount: Int,
    var archived: Boolean,
    var carrier: String,
    var fuelLiters: Double,
    var minimum: Double,
    var amount: Double,
    var attempts: Int,
    var retries: Int,
    var status: DropTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropTargetStatus): Boolean {
        when (status) {
            DropTargetStatus.REQUESTED -> {
                when (next) {
                    DropTargetStatus.REVOKED, DropTargetStatus.EXPIRED -> return true
                }
                return false
            }
            DropTargetStatus.GRANTED -> {
                when (next) {
                    DropTargetStatus.REVOKED, DropTargetStatus.EXPIRED -> return true
                }
                return false
            }
            DropTargetStatus.REVOKED -> {
                when (next) {
                    DropTargetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropTargetStatus) {
        if (!canMoveTo(next)) {
            error("drop target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round((fuelLiters + attempts.toDouble()) * 100) / 100

    fun snapshot(): DropTarget {
        return DropTarget(
            code = code,
            title = title,
            stopCount = stopCount,
            archived = archived,
            carrier = carrier,
            fuelLiters = fuelLiters,
            minimum = minimum,
            amount = amount,
            attempts = attempts,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a drop target in status requested. */
fun newDropTarget(code: String, title: String): DropTarget {
    return DropTarget(
        code = code,
        title = title,
        stopCount = 0,
        archived = false,
        carrier = "",
        fuelLiters = 0.0,
        minimum = 0.0,
        amount = 0.0,
        attempts = 0,
        retries = 0,
        status = DropTargetStatus.REQUESTED,
    )
}
