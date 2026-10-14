package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub adjustment is in its life cycle. */
enum class HubAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a hub adjustment and its current status. */
data class HubAdjustment(
    var code: String,
    var name: String,
    var timezone: String,
    var attempts: Int,
    var quantity: Int,
    var status: HubAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubAdjustmentStatus): Boolean {
        when (status) {
            HubAdjustmentStatus.NEW -> return next == HubAdjustmentStatus.DONE || next == HubAdjustmentStatus.FAILED
            HubAdjustmentStatus.QUEUED -> return next == HubAdjustmentStatus.DONE || next == HubAdjustmentStatus.FAILED
            HubAdjustmentStatus.RUNNING -> return next == HubAdjustmentStatus.DONE
            HubAdjustmentStatus.DONE -> {
                when (next) {
                    HubAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("hub adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round(attempts.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): HubAdjustment {
        return HubAdjustment(
            code = code,
            name = name,
            timezone = timezone,
            attempts = attempts,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hub adjustment in status new. */
fun newHubAdjustment(code: String, name: String): HubAdjustment {
    return HubAdjustment(
        code = code,
        name = name,
        timezone = "",
        attempts = 0,
        quantity = 0,
        status = HubAdjustmentStatus.NEW,
    )
}
