package com.acme.transport.freightpay.dispute.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute request is in its life cycle. */
enum class DisputeRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One dispute request, with what the planners need to act on it. */
data class DisputeRequest(
    var code: String,
    var name: String,
    var approved: Boolean,
    var revision: Int,
    var driverId: String,
    var rate: Double,
    var odometer: Int,
    var sequence: Int,
    var status: DisputeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeRequestStatus): Boolean {
        when (status) {
            DisputeRequestStatus.REQUESTED -> return next == DisputeRequestStatus.GRANTED || next == DisputeRequestStatus.REVOKED
            DisputeRequestStatus.GRANTED -> return next == DisputeRequestStatus.REVOKED || next == DisputeRequestStatus.EXPIRED
            DisputeRequestStatus.REVOKED -> {
                when (next) {
                    DisputeRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeRequestStatus) {
        if (!canMoveTo(next)) {
            error("dispute request $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun netValue(): Double = round((revision.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): DisputeRequest {
        return DisputeRequest(
            code = code,
            name = name,
            approved = approved,
            revision = revision,
            driverId = driverId,
            rate = rate,
            odometer = odometer,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dispute request in status requested. */
fun newDisputeRequest(code: String, name: String): DisputeRequest {
    return DisputeRequest(
        code = code,
        name = name,
        approved = false,
        revision = 0,
        driverId = "",
        rate = 0.0,
        odometer = 0,
        sequence = 0,
        status = DisputeRequestStatus.REQUESTED,
    )
}
