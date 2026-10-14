package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a accessorial estimate is in its life cycle. */
enum class AccessorialEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A accessorial estimate as the accessorial team keeps it. */
data class AccessorialEstimate(
    var code: String,
    var title: String,
    var channel: String,
    var capacity: Int,
    var attributes: MutableMap<String, String>,
    var cost: Double,
    var expiresAt: Int,
    var driverId: String,
    var status: AccessorialEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialEstimateStatus): Boolean {
        when (status) {
            AccessorialEstimateStatus.CREATED -> return next == AccessorialEstimateStatus.VALIDATED
            AccessorialEstimateStatus.VALIDATED -> {
                when (next) {
                    AccessorialEstimateStatus.PUBLISHED -> return true
                }
                return false
            }
            AccessorialEstimateStatus.PUBLISHED -> return next == AccessorialEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AccessorialEstimateStatus) {
        if (!canMoveTo(next)) {
            error("accessorial estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedExposure(): Double = round((cost + capacity.toDouble()) * 100) / 100

    fun snapshot(): AccessorialEstimate {
        return AccessorialEstimate(
            code = code,
            title = title,
            channel = channel,
            capacity = capacity,
            attributes = attributes,
            cost = cost,
            expiresAt = expiresAt,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a accessorial estimate in status created. */
fun newAccessorialEstimate(code: String, title: String): AccessorialEstimate {
    return AccessorialEstimate(
        code = code,
        title = title,
        channel = "",
        capacity = 0,
        attributes = mutableMapOf<String, String>(),
        cost = 0.0,
        expiresAt = 0,
        driverId = "",
        status = AccessorialEstimateStatus.CREATED,
    )
}
