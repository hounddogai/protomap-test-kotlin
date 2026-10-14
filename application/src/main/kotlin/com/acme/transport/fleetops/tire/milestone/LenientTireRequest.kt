package com.acme.transport.fleetops.tire.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient tire request is in its life cycle. */
enum class LenientTireRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient tire request as the tire team keeps it. */
data class LenientTireRequest(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var driverId: String,
    var score: Double,
    var country: String,
    var sequence: Int,
    var ceiling: Double,
    var odometer: Int,
    var status: LenientTireRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTireRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTireRequestStatus): Boolean {
        when (status) {
            LenientTireRequestStatus.DRAFT -> {
                when (next) {
                    LenientTireRequestStatus.PENDING, LenientTireRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientTireRequestStatus.PENDING -> {
                when (next) {
                    LenientTireRequestStatus.ACTIVE, LenientTireRequestStatus.CLOSED -> return true
                }
                return false
            }
            LenientTireRequestStatus.ACTIVE -> {
                when (next) {
                    LenientTireRequestStatus.SUSPENDED, LenientTireRequestStatus.CLOSED -> return true
                }
                return false
            }
            LenientTireRequestStatus.SUSPENDED -> {
                when (next) {
                    LenientTireRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientTireRequestStatus) {
        if (!canMoveTo(next)) {
            error("lenient tire request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient tire request"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((ceiling + odometer.toDouble()) * 100) / 100

    fun snapshot(): LenientTireRequest {
        return LenientTireRequest(
            code = code,
            title = title,
            attributes = attributes,
            driverId = driverId,
            score = score,
            country = country,
            sequence = sequence,
            ceiling = ceiling,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient tire request in status draft. */
fun newLenientTireRequest(code: String, title: String): LenientTireRequest {
    return LenientTireRequest(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        driverId = "",
        score = 0.0,
        country = "",
        sequence = 0,
        ceiling = 0.0,
        odometer = 0,
        status = LenientTireRequestStatus.DRAFT,
    )
}
