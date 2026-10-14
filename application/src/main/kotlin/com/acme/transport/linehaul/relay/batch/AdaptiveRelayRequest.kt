package com.acme.transport.linehaul.relay.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive relay request is in its life cycle. */
enum class AdaptiveRelayRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A adaptive relay request. Its code is unique within a tenant. */
data class AdaptiveRelayRequest(
    var code: String,
    var name: String,
    var currency: String,
    var active: Boolean,
    var startsAt: Int,
    var sequence: Int,
    var status: AdaptiveRelayRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRelayRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRelayRequestStatus): Boolean {
        when (status) {
            AdaptiveRelayRequestStatus.PROPOSED -> return next == AdaptiveRelayRequestStatus.APPROVED || next == AdaptiveRelayRequestStatus.REJECTED
            AdaptiveRelayRequestStatus.APPROVED -> return next == AdaptiveRelayRequestStatus.REJECTED || next == AdaptiveRelayRequestStatus.WITHDRAWN
            AdaptiveRelayRequestStatus.REJECTED -> {
                when (next) {
                    AdaptiveRelayRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRelayRequestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive relay request $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedVolume(): Double = round((sequence.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRelayRequest {
        return AdaptiveRelayRequest(
            code = code,
            name = name,
            currency = currency,
            active = active,
            startsAt = startsAt,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive relay request in status proposed. */
fun newAdaptiveRelayRequest(code: String, name: String): AdaptiveRelayRequest {
    return AdaptiveRelayRequest(
        code = code,
        name = name,
        currency = "",
        active = false,
        startsAt = 0,
        sequence = 0,
        status = AdaptiveRelayRequestStatus.PROPOSED,
    )
}
