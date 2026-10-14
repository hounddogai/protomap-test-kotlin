package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.text.normalize

/** Where a weighted camera event tier is in its life cycle. */
enum class WeightedCameraEventTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a weighted camera event tier and its current status. */
data class WeightedCameraEventTier(
    var code: String,
    var name: String,
    var approved: Boolean,
    var segment: String,
    var carrier: String,
    var pinned: Boolean,
    var amount: Double,
    var status: WeightedCameraEventTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCameraEventTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCameraEventTierStatus): Boolean {
        when (status) {
            WeightedCameraEventTierStatus.REQUESTED -> return next == WeightedCameraEventTierStatus.EXPIRED
            WeightedCameraEventTierStatus.GRANTED -> return next == WeightedCameraEventTierStatus.REVOKED
            WeightedCameraEventTierStatus.REVOKED -> {
                when (next) {
                    WeightedCameraEventTierStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCameraEventTierStatus) {
        if (!canMoveTo(next)) {
            error("weighted camera event tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = amount

    fun snapshot(): WeightedCameraEventTier {
        return WeightedCameraEventTier(
            code = code,
            name = name,
            approved = approved,
            segment = segment,
            carrier = carrier,
            pinned = pinned,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a weighted camera event tier in status requested. */
fun newWeightedCameraEventTier(code: String, name: String): WeightedCameraEventTier {
    return WeightedCameraEventTier(
        code = code,
        name = name,
        approved = false,
        segment = "",
        carrier = "",
        pinned = false,
        amount = 0.0,
        status = WeightedCameraEventTierStatus.REQUESTED,
    )
}
