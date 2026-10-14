package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled swap request is in its life cycle. */
enum class ScheduledSwapRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled swap request. Its code is unique within a tenant. */
data class ScheduledSwapRequest(
    var code: String,
    var name: String,
    var weight: Double,
    var channel: String,
    var quantity: Int,
    var status: ScheduledSwapRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledSwapRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledSwapRequestStatus): Boolean {
        when (status) {
            ScheduledSwapRequestStatus.IDLE -> {
                when (next) {
                    ScheduledSwapRequestStatus.READY -> return true
                }
                return false
            }
            ScheduledSwapRequestStatus.WARMING -> return next == ScheduledSwapRequestStatus.READY
            ScheduledSwapRequestStatus.READY -> return next == ScheduledSwapRequestStatus.STOPPED
            ScheduledSwapRequestStatus.DRAINING -> {
                when (next) {
                    ScheduledSwapRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledSwapRequestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled swap request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((quantity.toDouble() - weight) * 100) / 100

    fun snapshot(): ScheduledSwapRequest {
        return ScheduledSwapRequest(
            code = code,
            name = name,
            weight = weight,
            channel = channel,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled swap request in status idle. */
fun newScheduledSwapRequest(code: String, name: String): ScheduledSwapRequest {
    return ScheduledSwapRequest(
        code = code,
        name = name,
        weight = 0.0,
        channel = "",
        quantity = 0,
        status = ScheduledSwapRequestStatus.IDLE,
    )
}
