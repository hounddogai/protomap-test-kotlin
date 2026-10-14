package com.acme.transport.network.hub.hubsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default hub tier is in its life cycle. */
enum class DefaultHubTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a default hub tier and its current status. */
data class DefaultHubTier(
    var code: String,
    var title: String,
    var sequence: Int,
    var distanceKm: Double,
    var lane: String,
    var locale: String,
    var tags: MutableList<String>,
    var driverId: String,
    var minimum: Double,
    var shiftCode: String,
    var cost: Double,
    var status: DefaultHubTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHubTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHubTierStatus): Boolean {
        when (status) {
            DefaultHubTierStatus.PROPOSED -> {
                when (next) {
                    DefaultHubTierStatus.REJECTED -> return true
                }
                return false
            }
            DefaultHubTierStatus.APPROVED -> return next == DefaultHubTierStatus.REJECTED || next == DefaultHubTierStatus.WITHDRAWN
            DefaultHubTierStatus.REJECTED -> {
                when (next) {
                    DefaultHubTierStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultHubTierStatus) {
        if (!canMoveTo(next)) {
            error("default hub tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round(cost * distanceKm * 100) / 100

    fun snapshot(): DefaultHubTier {
        return DefaultHubTier(
            code = code,
            title = title,
            sequence = sequence,
            distanceKm = distanceKm,
            lane = lane,
            locale = locale,
            tags = tags,
            driverId = driverId,
            minimum = minimum,
            shiftCode = shiftCode,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default hub tier in status proposed. */
fun newDefaultHubTier(code: String, title: String): DefaultHubTier {
    return DefaultHubTier(
        code = code,
        title = title,
        sequence = 0,
        distanceKm = 0.0,
        lane = "",
        locale = "",
        tags = mutableListOf<String>(),
        driverId = "",
        minimum = 0.0,
        shiftCode = "",
        cost = 0.0,
        status = DefaultHubTierStatus.PROPOSED,
    )
}
