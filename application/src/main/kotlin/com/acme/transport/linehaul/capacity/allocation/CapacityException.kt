package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity exception is in its life cycle. */
enum class CapacityExceptionStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capacity exception and its current status. */
data class CapacityException(
    var code: String,
    var name: String,
    var startsAt: Int,
    var depot: String,
    var channel: String,
    var reviewer: String,
    var vehicleId: String,
    var weight: Double,
    var quantity: Int,
    var approved: Boolean,
    var status: CapacityExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityExceptionStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityExceptionStatus): Boolean {
        when (status) {
            CapacityExceptionStatus.CREATED -> {
                when (next) {
                    CapacityExceptionStatus.PUBLISHED -> return true
                }
                return false
            }
            CapacityExceptionStatus.VALIDATED -> return next == CapacityExceptionStatus.PUBLISHED
            CapacityExceptionStatus.PUBLISHED -> return next == CapacityExceptionStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CapacityExceptionStatus) {
        if (!canMoveTo(next)) {
            error("capacity exception $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round((quantity.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): CapacityException {
        return CapacityException(
            code = code,
            name = name,
            startsAt = startsAt,
            depot = depot,
            channel = channel,
            reviewer = reviewer,
            vehicleId = vehicleId,
            weight = weight,
            quantity = quantity,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capacity exception in status created. */
fun newCapacityException(code: String, name: String): CapacityException {
    return CapacityException(
        code = code,
        name = name,
        startsAt = 0,
        depot = "",
        channel = "",
        reviewer = "",
        vehicleId = "",
        weight = 0.0,
        quantity = 0,
        approved = false,
        status = CapacityExceptionStatus.CREATED,
    )
}
