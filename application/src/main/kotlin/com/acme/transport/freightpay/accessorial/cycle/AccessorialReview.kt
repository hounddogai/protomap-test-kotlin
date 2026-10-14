package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial review is in its life cycle. */
enum class AccessorialReviewStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a accessorial review and its current status. */
data class AccessorialReview(
    var code: String,
    var title: String,
    var ratio: Double,
    var dock: String,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var odometer: Int,
    var ceiling: Double,
    var vehicleId: String,
    var maximum: Double,
    var budget: Double,
    var status: AccessorialReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialReviewStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialReviewStatus): Boolean {
        when (status) {
            AccessorialReviewStatus.DRAFT -> {
                when (next) {
                    AccessorialReviewStatus.PENDING -> return true
                }
                return false
            }
            AccessorialReviewStatus.PENDING -> return next == AccessorialReviewStatus.CLOSED
            AccessorialReviewStatus.ACTIVE -> {
                when (next) {
                    AccessorialReviewStatus.SUSPENDED, AccessorialReviewStatus.CLOSED -> return true
                }
                return false
            }
            AccessorialReviewStatus.SUSPENDED -> {
                when (next) {
                    AccessorialReviewStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialReviewStatus) {
        if (!canMoveTo(next)) {
            error("accessorial review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((odometer.toDouble() - ceiling) * 100) / 100

    fun snapshot(): AccessorialReview {
        return AccessorialReview(
            code = code,
            title = title,
            ratio = ratio,
            dock = dock,
            attributes = attributes,
            active = active,
            odometer = odometer,
            ceiling = ceiling,
            vehicleId = vehicleId,
            maximum = maximum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a accessorial review in status draft. */
fun newAccessorialReview(code: String, title: String): AccessorialReview {
    return AccessorialReview(
        code = code,
        title = title,
        ratio = 0.0,
        dock = "",
        attributes = mutableMapOf<String, String>(),
        active = false,
        odometer = 0,
        ceiling = 0.0,
        vehicleId = "",
        maximum = 0.0,
        budget = 0.0,
        status = AccessorialReviewStatus.DRAFT,
    )
}
