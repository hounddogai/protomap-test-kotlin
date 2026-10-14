package com.acme.transport.emissions.idling.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive idling digest is in its life cycle. */
enum class AdaptiveIdlingDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive idling digest as the idling team keeps it. */
data class AdaptiveIdlingDigest(
    var code: String,
    var title: String,
    var quantity: Int,
    var fuelLiters: Double,
    var threshold: Double,
    var country: String,
    var margin: Double,
    var category: String,
    var ceiling: Double,
    var odometer: Int,
    var status: AdaptiveIdlingDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIdlingDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIdlingDigestStatus): Boolean {
        when (status) {
            AdaptiveIdlingDigestStatus.DRAFT -> {
                when (next) {
                    AdaptiveIdlingDigestStatus.PENDING -> return true
                }
                return false
            }
            AdaptiveIdlingDigestStatus.PENDING -> return next == AdaptiveIdlingDigestStatus.SUSPENDED
            AdaptiveIdlingDigestStatus.ACTIVE -> {
                when (next) {
                    AdaptiveIdlingDigestStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveIdlingDigestStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveIdlingDigestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveIdlingDigestStatus) {
        if (!canMoveTo(next)) {
            error("adaptive idling digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive idling digest"} $code"

    fun effectiveTotal(): Double = round((margin - odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveIdlingDigest {
        return AdaptiveIdlingDigest(
            code = code,
            title = title,
            quantity = quantity,
            fuelLiters = fuelLiters,
            threshold = threshold,
            country = country,
            margin = margin,
            category = category,
            ceiling = ceiling,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive idling digest in status draft. */
fun newAdaptiveIdlingDigest(code: String, title: String): AdaptiveIdlingDigest {
    return AdaptiveIdlingDigest(
        code = code,
        title = title,
        quantity = 0,
        fuelLiters = 0.0,
        threshold = 0.0,
        country = "",
        margin = 0.0,
        category = "",
        ceiling = 0.0,
        odometer = 0,
        status = AdaptiveIdlingDigestStatus.DRAFT,
    )
}
