package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.text.normalize

/** Where a default trailer allocation is in its life cycle. */
enum class DefaultTrailerAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default trailer allocation as the trailer team keeps it. */
data class DefaultTrailerAllocation(
    var code: String,
    var name: String,
    var country: String,
    var lane: String,
    var score: Double,
    var status: DefaultTrailerAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTrailerAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTrailerAllocationStatus): Boolean {
        when (status) {
            DefaultTrailerAllocationStatus.IDLE -> return next == DefaultTrailerAllocationStatus.WARMING
            DefaultTrailerAllocationStatus.WARMING -> {
                when (next) {
                    DefaultTrailerAllocationStatus.READY, DefaultTrailerAllocationStatus.DRAINING -> return true
                }
                return false
            }
            DefaultTrailerAllocationStatus.READY -> return next == DefaultTrailerAllocationStatus.DRAINING || next == DefaultTrailerAllocationStatus.STOPPED
            DefaultTrailerAllocationStatus.DRAINING -> return next == DefaultTrailerAllocationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultTrailerAllocationStatus) {
        if (!canMoveTo(next)) {
            error("default trailer allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default trailer allocation"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): DefaultTrailerAllocation {
        return DefaultTrailerAllocation(
            code = code,
            name = name,
            country = country,
            lane = lane,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a default trailer allocation in status idle. */
fun newDefaultTrailerAllocation(code: String, name: String): DefaultTrailerAllocation {
    return DefaultTrailerAllocation(
        code = code,
        name = name,
        country = "",
        lane = "",
        score = 0.0,
        status = DefaultTrailerAllocationStatus.IDLE,
    )
}
