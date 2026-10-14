package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global dispute request is in its life cycle. */
enum class GlobalDisputeRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One global dispute request, with what the planners need to act on it. */
data class GlobalDisputeRequest(
    var code: String,
    var name: String,
    var minimum: Double,
    var channel: String,
    var rate: Double,
    var ratio: Double,
    var description: String,
    var status: GlobalDisputeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDisputeRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDisputeRequestStatus): Boolean {
        when (status) {
            GlobalDisputeRequestStatus.PLANNED -> return next == GlobalDisputeRequestStatus.CANCELLED
            GlobalDisputeRequestStatus.SCHEDULED -> return next == GlobalDisputeRequestStatus.IN_PROGRESS || next == GlobalDisputeRequestStatus.CANCELLED
            GlobalDisputeRequestStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalDisputeRequestStatus.CANCELLED -> return true
                }
                return false
            }
            GlobalDisputeRequestStatus.COMPLETED -> return next == GlobalDisputeRequestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalDisputeRequestStatus) {
        if (!canMoveTo(next)) {
            error("global dispute request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((rate - minimum) * 100) / 100

    fun snapshot(): GlobalDisputeRequest {
        return GlobalDisputeRequest(
            code = code,
            name = name,
            minimum = minimum,
            channel = channel,
            rate = rate,
            ratio = ratio,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global dispute request in status planned. */
fun newGlobalDisputeRequest(code: String, name: String): GlobalDisputeRequest {
    return GlobalDisputeRequest(
        code = code,
        name = name,
        minimum = 0.0,
        channel = "",
        rate = 0.0,
        ratio = 0.0,
        description = "",
        status = GlobalDisputeRequestStatus.PLANNED,
    )
}
