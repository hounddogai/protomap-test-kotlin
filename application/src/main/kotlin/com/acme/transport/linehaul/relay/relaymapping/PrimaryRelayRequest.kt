package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary relay request is in its life cycle. */
enum class PrimaryRelayRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One primary relay request, with what the planners need to act on it. */
data class PrimaryRelayRequest(
    var code: String,
    var title: String,
    var retries: Int,
    var odometer: Int,
    var weight: Double,
    var name: String,
    var distanceKm: Double,
    var budget: Double,
    var checksum: String,
    var currency: String,
    var status: PrimaryRelayRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRelayRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRelayRequestStatus): Boolean {
        when (status) {
            PrimaryRelayRequestStatus.CREATED -> return next == PrimaryRelayRequestStatus.PUBLISHED || next == PrimaryRelayRequestStatus.RETIRED
            PrimaryRelayRequestStatus.VALIDATED -> return next == PrimaryRelayRequestStatus.RETIRED
            PrimaryRelayRequestStatus.PUBLISHED -> {
                when (next) {
                    PrimaryRelayRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRelayRequestStatus) {
        if (!canMoveTo(next)) {
            error("primary relay request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((odometer.toDouble() + budget) * 100) / 100

    fun snapshot(): PrimaryRelayRequest {
        return PrimaryRelayRequest(
            code = code,
            title = title,
            retries = retries,
            odometer = odometer,
            weight = weight,
            name = name,
            distanceKm = distanceKm,
            budget = budget,
            checksum = checksum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary relay request in status created. */
fun newPrimaryRelayRequest(code: String, title: String): PrimaryRelayRequest {
    return PrimaryRelayRequest(
        code = code,
        title = title,
        retries = 0,
        odometer = 0,
        weight = 0.0,
        name = "",
        distanceKm = 0.0,
        budget = 0.0,
        checksum = "",
        currency = "",
        status = PrimaryRelayRequestStatus.CREATED,
    )
}
