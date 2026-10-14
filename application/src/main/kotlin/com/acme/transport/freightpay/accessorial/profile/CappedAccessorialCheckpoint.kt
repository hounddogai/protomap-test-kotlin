package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped accessorial checkpoint is in its life cycle. */
enum class CappedAccessorialCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capped accessorial checkpoint and its current status. */
data class CappedAccessorialCheckpoint(
    var code: String,
    var name: String,
    var priority: Int,
    var carrier: String,
    var title: String,
    var routeCode: String,
    var quantity: Int,
    var status: CappedAccessorialCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAccessorialCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAccessorialCheckpointStatus): Boolean {
        when (status) {
            CappedAccessorialCheckpointStatus.IDLE -> return next == CappedAccessorialCheckpointStatus.STOPPED
            CappedAccessorialCheckpointStatus.WARMING -> return next == CappedAccessorialCheckpointStatus.DRAINING || next == CappedAccessorialCheckpointStatus.STOPPED
            CappedAccessorialCheckpointStatus.READY -> {
                when (next) {
                    CappedAccessorialCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            CappedAccessorialCheckpointStatus.DRAINING -> return next == CappedAccessorialCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedAccessorialCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capped accessorial checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveExposure(): Double = round((priority.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): CappedAccessorialCheckpoint {
        return CappedAccessorialCheckpoint(
            code = code,
            name = name,
            priority = priority,
            carrier = carrier,
            title = title,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped accessorial checkpoint in status idle. */
fun newCappedAccessorialCheckpoint(code: String, name: String): CappedAccessorialCheckpoint {
    return CappedAccessorialCheckpoint(
        code = code,
        name = name,
        priority = 0,
        carrier = "",
        title = "",
        routeCode = "",
        quantity = 0,
        status = CappedAccessorialCheckpointStatus.IDLE,
    )
}
