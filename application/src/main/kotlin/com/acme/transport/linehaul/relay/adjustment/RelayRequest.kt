package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.kit.text.normalize

/** Where a relay request is in its life cycle. */
enum class RelayRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a relay request and its current status. */
data class RelayRequest(
    var code: String,
    var title: String,
    var externalRef: String,
    var endsAt: Int,
    var vehicleId: String,
    var status: RelayRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayRequestStatus): Boolean {
        when (status) {
            RelayRequestStatus.PROPOSED -> {
                when (next) {
                    RelayRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            RelayRequestStatus.APPROVED -> return next == RelayRequestStatus.REJECTED || next == RelayRequestStatus.WITHDRAWN
            RelayRequestStatus.REJECTED -> return next == RelayRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RelayRequestStatus) {
        if (!canMoveTo(next)) {
            error("relay request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): RelayRequest {
        return RelayRequest(
            code = code,
            title = title,
            externalRef = externalRef,
            endsAt = endsAt,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a relay request in status proposed. */
fun newRelayRequest(code: String, title: String): RelayRequest {
    return RelayRequest(
        code = code,
        title = title,
        externalRef = "",
        endsAt = 0,
        vehicleId = "",
        status = RelayRequestStatus.PROPOSED,
    )
}
