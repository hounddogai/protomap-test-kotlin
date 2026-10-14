package com.acme.transport.customs.broker.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback broker bundle is in its life cycle. */
enum class FallbackBrokerBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback broker bundle as the broker team keeps it. */
data class FallbackBrokerBundle(
    var code: String,
    var name: String,
    var minimum: Double,
    var visible: Boolean,
    var country: String,
    var durationMinutes: Int,
    var graceDays: Int,
    var sequence: Int,
    var loadKg: Double,
    var maximum: Double,
    var status: FallbackBrokerBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackBrokerBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackBrokerBundleStatus): Boolean {
        when (status) {
            FallbackBrokerBundleStatus.PROPOSED -> {
                when (next) {
                    FallbackBrokerBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackBrokerBundleStatus.APPROVED -> return next == FallbackBrokerBundleStatus.WITHDRAWN
            FallbackBrokerBundleStatus.REJECTED -> {
                when (next) {
                    FallbackBrokerBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackBrokerBundleStatus) {
        if (!canMoveTo(next)) {
            error("fallback broker bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback broker bundle"} $code"

    fun effectiveExposure(): Double = round((loadKg - graceDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackBrokerBundle {
        return FallbackBrokerBundle(
            code = code,
            name = name,
            minimum = minimum,
            visible = visible,
            country = country,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            sequence = sequence,
            loadKg = loadKg,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback broker bundle in status proposed. */
fun newFallbackBrokerBundle(code: String, name: String): FallbackBrokerBundle {
    return FallbackBrokerBundle(
        code = code,
        name = name,
        minimum = 0.0,
        visible = false,
        country = "",
        durationMinutes = 0,
        graceDays = 0,
        sequence = 0,
        loadKg = 0.0,
        maximum = 0.0,
        status = FallbackBrokerBundleStatus.PROPOSED,
    )
}
