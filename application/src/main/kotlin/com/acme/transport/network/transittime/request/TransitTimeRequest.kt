package com.acme.transport.network.transittime.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time request records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time request is in its life cycle. */
enum class TransitTimeRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A transit time request. Its code is unique within a tenant. */
data class TransitTimeRequest(
    var code: String,
    var title: String,
    var currency: String,
    var shiftCode: String,
    var attributes: MutableMap<String, String>,
    var ratio: Double,
    var distanceKm: Double,
    var category: String,
    var margin: Double,
    var status: TransitTimeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeRequestStatus): Boolean {
        when (status) {
            TransitTimeRequestStatus.REQUESTED -> return next == TransitTimeRequestStatus.EXPIRED
            TransitTimeRequestStatus.GRANTED -> {
                when (next) {
                    TransitTimeRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TransitTimeRequestStatus.REVOKED -> {
                when (next) {
                    TransitTimeRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeRequestStatus) {
        if (!canMoveTo(next)) {
            error("transit time request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round(ratio * margin * 100) / 100

    fun snapshot(): TransitTimeRequest {
        return TransitTimeRequest(
            code = code,
            title = title,
            currency = currency,
            shiftCode = shiftCode,
            attributes = attributes,
            ratio = ratio,
            distanceKm = distanceKm,
            category = category,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a transit time request in status requested. */
fun newTransitTimeRequest(code: String, title: String): TransitTimeRequest {
    return TransitTimeRequest(
        code = code,
        title = title,
        currency = "",
        shiftCode = "",
        attributes = mutableMapOf<String, String>(),
        ratio = 0.0,
        distanceKm = 0.0,
        category = "",
        margin = 0.0,
        status = TransitTimeRequestStatus.REQUESTED,
    )
}
