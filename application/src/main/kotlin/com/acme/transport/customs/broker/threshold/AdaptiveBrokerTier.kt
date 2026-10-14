package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive broker tier is in its life cycle. */
enum class AdaptiveBrokerTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a adaptive broker tier and its current status. */
data class AdaptiveBrokerTier(
    var code: String,
    var name: String,
    var discount: Double,
    var capacity: Int,
    var checksum: String,
    var priority: Int,
    var loadKg: Double,
    var graceDays: Int,
    var country: String,
    var status: AdaptiveBrokerTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveBrokerTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveBrokerTierStatus): Boolean {
        when (status) {
            AdaptiveBrokerTierStatus.OPEN -> return next == AdaptiveBrokerTierStatus.IN_REVIEW || next == AdaptiveBrokerTierStatus.REOPENED
            AdaptiveBrokerTierStatus.IN_REVIEW -> return next == AdaptiveBrokerTierStatus.REOPENED || next == AdaptiveBrokerTierStatus.ARCHIVED
            AdaptiveBrokerTierStatus.RESOLVED -> {
                when (next) {
                    AdaptiveBrokerTierStatus.REOPENED -> return true
                }
                return false
            }
            AdaptiveBrokerTierStatus.REOPENED -> {
                when (next) {
                    AdaptiveBrokerTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveBrokerTierStatus) {
        if (!canMoveTo(next)) {
            error("adaptive broker tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive broker tier"} $code"

    fun adjustedTotal(): Double = round((graceDays.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveBrokerTier {
        return AdaptiveBrokerTier(
            code = code,
            name = name,
            discount = discount,
            capacity = capacity,
            checksum = checksum,
            priority = priority,
            loadKg = loadKg,
            graceDays = graceDays,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive broker tier in status open. */
fun newAdaptiveBrokerTier(code: String, name: String): AdaptiveBrokerTier {
    return AdaptiveBrokerTier(
        code = code,
        name = name,
        discount = 0.0,
        capacity = 0,
        checksum = "",
        priority = 0,
        loadKg = 0.0,
        graceDays = 0,
        country = "",
        status = AdaptiveBrokerTierStatus.OPEN,
    )
}
