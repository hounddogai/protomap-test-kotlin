package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery score is in its life cycle. */
enum class RedeliveryScoreStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A redelivery score as the redelivery team keeps it. */
data class RedeliveryScore(
    var code: String,
    var title: String,
    var visible: Boolean,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var odometer: Int,
    var routeCode: String,
    var ownerId: String,
    var budget: Double,
    var carrier: String,
    var retries: Int,
    var status: RedeliveryScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryScoreStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryScoreStatus): Boolean {
        when (status) {
            RedeliveryScoreStatus.CREATED -> return next == RedeliveryScoreStatus.VALIDATED || next == RedeliveryScoreStatus.RETIRED
            RedeliveryScoreStatus.VALIDATED -> {
                when (next) {
                    RedeliveryScoreStatus.RETIRED -> return true
                }
                return false
            }
            RedeliveryScoreStatus.PUBLISHED -> return next == RedeliveryScoreStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RedeliveryScoreStatus) {
        if (!canMoveTo(next)) {
            error("redelivery score $code cannot move from $status to $next")
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

    fun netExposure(): Double = round((budget + odometer.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryScore {
        return RedeliveryScore(
            code = code,
            title = title,
            visible = visible,
            attributes = attributes,
            vehicleId = vehicleId,
            odometer = odometer,
            routeCode = routeCode,
            ownerId = ownerId,
            budget = budget,
            carrier = carrier,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a redelivery score in status created. */
fun newRedeliveryScore(code: String, title: String): RedeliveryScore {
    return RedeliveryScore(
        code = code,
        title = title,
        visible = false,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        odometer = 0,
        routeCode = "",
        ownerId = "",
        budget = 0.0,
        carrier = "",
        retries = 0,
        status = RedeliveryScoreStatus.CREATED,
    )
}
