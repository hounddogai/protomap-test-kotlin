package com.acme.transport.crewing.availability.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted availability digest is in its life cycle. */
enum class WeightedAvailabilityDigestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted availability digest as the availability team keeps it. */
data class WeightedAvailabilityDigest(
    var code: String,
    var title: String,
    var depot: String,
    var graceDays: Int,
    var maximum: Double,
    var amount: Double,
    var locale: String,
    var startsAt: Int,
    var minimum: Double,
    var quantity: Int,
    var weight: Double,
    var status: WeightedAvailabilityDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAvailabilityDigestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAvailabilityDigestStatus): Boolean {
        when (status) {
            WeightedAvailabilityDigestStatus.REQUESTED -> {
                when (next) {
                    WeightedAvailabilityDigestStatus.REVOKED -> return true
                }
                return false
            }
            WeightedAvailabilityDigestStatus.GRANTED -> {
                when (next) {
                    WeightedAvailabilityDigestStatus.REVOKED -> return true
                }
                return false
            }
            WeightedAvailabilityDigestStatus.REVOKED -> return next == WeightedAvailabilityDigestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedAvailabilityDigestStatus) {
        if (!canMoveTo(next)) {
            error("weighted availability digest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round(amount * startsAt.toDouble() * 100) / 100

    fun snapshot(): WeightedAvailabilityDigest {
        return WeightedAvailabilityDigest(
            code = code,
            title = title,
            depot = depot,
            graceDays = graceDays,
            maximum = maximum,
            amount = amount,
            locale = locale,
            startsAt = startsAt,
            minimum = minimum,
            quantity = quantity,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted availability digest in status requested. */
fun newWeightedAvailabilityDigest(code: String, title: String): WeightedAvailabilityDigest {
    return WeightedAvailabilityDigest(
        code = code,
        title = title,
        depot = "",
        graceDays = 0,
        maximum = 0.0,
        amount = 0.0,
        locale = "",
        startsAt = 0,
        minimum = 0.0,
        quantity = 0,
        weight = 0.0,
        status = WeightedAvailabilityDigestStatus.REQUESTED,
    )
}
