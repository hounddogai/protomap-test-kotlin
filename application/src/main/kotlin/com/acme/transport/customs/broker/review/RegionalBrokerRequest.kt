package com.acme.transport.customs.broker.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional broker request is in its life cycle. */
enum class RegionalBrokerRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional broker request as the broker team keeps it. */
data class RegionalBrokerRequest(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var ceiling: Double,
    var vehicleId: String,
    var limit: Int,
    var sequence: Int,
    var name: String,
    var minimum: Double,
    var status: RegionalBrokerRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalBrokerRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalBrokerRequestStatus): Boolean {
        when (status) {
            RegionalBrokerRequestStatus.PLANNED -> {
                when (next) {
                    RegionalBrokerRequestStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalBrokerRequestStatus.SCHEDULED -> {
                when (next) {
                    RegionalBrokerRequestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RegionalBrokerRequestStatus.IN_PROGRESS -> return next == RegionalBrokerRequestStatus.CANCELLED
            RegionalBrokerRequestStatus.COMPLETED -> {
                when (next) {
                    RegionalBrokerRequestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalBrokerRequestStatus) {
        if (!canMoveTo(next)) {
            error("regional broker request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((minimum + ceiling) * 100) / 100

    fun snapshot(): RegionalBrokerRequest {
        return RegionalBrokerRequest(
            code = code,
            title = title,
            distanceKm = distanceKm,
            ceiling = ceiling,
            vehicleId = vehicleId,
            limit = limit,
            sequence = sequence,
            name = name,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional broker request in status planned. */
fun newRegionalBrokerRequest(code: String, title: String): RegionalBrokerRequest {
    return RegionalBrokerRequest(
        code = code,
        title = title,
        distanceKm = 0.0,
        ceiling = 0.0,
        vehicleId = "",
        limit = 0,
        sequence = 0,
        name = "",
        minimum = 0.0,
        status = RegionalBrokerRequestStatus.PLANNED,
    )
}
