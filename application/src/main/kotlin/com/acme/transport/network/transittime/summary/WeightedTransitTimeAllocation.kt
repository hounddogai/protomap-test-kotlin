package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted transit time allocation is in its life cycle. */
enum class WeightedTransitTimeAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One weighted transit time allocation, with what the planners need to act on it. */
data class WeightedTransitTimeAllocation(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var amount: Double,
    var phone: String,
    var distanceKm: Double,
    var quantity: Int,
    var endsAt: Int,
    var tags: MutableList<String>,
    var ratio: Double,
    var status: WeightedTransitTimeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTransitTimeAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTransitTimeAllocationStatus): Boolean {
        when (status) {
            WeightedTransitTimeAllocationStatus.REQUESTED -> return next == WeightedTransitTimeAllocationStatus.GRANTED
            WeightedTransitTimeAllocationStatus.GRANTED -> return next == WeightedTransitTimeAllocationStatus.REVOKED
            WeightedTransitTimeAllocationStatus.REVOKED -> return next == WeightedTransitTimeAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedTransitTimeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("weighted transit time allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round(distanceKm * ratio * 100) / 100

    fun snapshot(): WeightedTransitTimeAllocation {
        return WeightedTransitTimeAllocation(
            code = code,
            name = name,
            updatedAt = updatedAt,
            amount = amount,
            phone = phone,
            distanceKm = distanceKm,
            quantity = quantity,
            endsAt = endsAt,
            tags = tags,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted transit time allocation in status requested. */
fun newWeightedTransitTimeAllocation(code: String, name: String): WeightedTransitTimeAllocation {
    return WeightedTransitTimeAllocation(
        code = code,
        name = name,
        updatedAt = 0,
        amount = 0.0,
        phone = "",
        distanceKm = 0.0,
        quantity = 0,
        endsAt = 0,
        tags = mutableListOf<String>(),
        ratio = 0.0,
        status = WeightedTransitTimeAllocationStatus.REQUESTED,
    )
}
