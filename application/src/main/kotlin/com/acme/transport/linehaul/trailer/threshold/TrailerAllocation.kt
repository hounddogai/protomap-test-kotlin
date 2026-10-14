package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer allocation is in its life cycle. */
enum class TrailerAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a trailer allocation and its current status. */
data class TrailerAllocation(
    var code: String,
    var name: String,
    var threshold: Double,
    var channel: String,
    var batchSize: Int,
    var ownerId: String,
    var endsAt: Int,
    var updatedAt: Int,
    var country: String,
    var active: Boolean,
    var status: TrailerAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerAllocationStatus): Boolean {
        when (status) {
            TrailerAllocationStatus.PROPOSED -> {
                when (next) {
                    TrailerAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            TrailerAllocationStatus.APPROVED -> {
                when (next) {
                    TrailerAllocationStatus.REJECTED -> return true
                }
                return false
            }
            TrailerAllocationStatus.REJECTED -> return next == TrailerAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TrailerAllocationStatus) {
        if (!canMoveTo(next)) {
            error("trailer allocation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"trailer allocation"} $code"

    fun projectedVolume(): Double = round((updatedAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): TrailerAllocation {
        return TrailerAllocation(
            code = code,
            name = name,
            threshold = threshold,
            channel = channel,
            batchSize = batchSize,
            ownerId = ownerId,
            endsAt = endsAt,
            updatedAt = updatedAt,
            country = country,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a trailer allocation in status proposed. */
fun newTrailerAllocation(code: String, name: String): TrailerAllocation {
    return TrailerAllocation(
        code = code,
        name = name,
        threshold = 0.0,
        channel = "",
        batchSize = 0,
        ownerId = "",
        endsAt = 0,
        updatedAt = 0,
        country = "",
        active = false,
        status = TrailerAllocationStatus.PROPOSED,
    )
}
