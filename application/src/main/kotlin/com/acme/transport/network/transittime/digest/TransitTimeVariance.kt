package com.acme.transport.network.transittime.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time variance is in its life cycle. */
enum class TransitTimeVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A transit time variance as the transit time team keeps it. */
data class TransitTimeVariance(
    var code: String,
    var title: String,
    var routeCode: String,
    var graceDays: Int,
    var fuelLiters: Double,
    var archived: Boolean,
    var unit: String,
    var attributes: MutableMap<String, String>,
    var margin: Double,
    var status: TransitTimeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeVarianceStatus): Boolean {
        when (status) {
            TransitTimeVarianceStatus.IDLE -> {
                when (next) {
                    TransitTimeVarianceStatus.DRAINING -> return true
                }
                return false
            }
            TransitTimeVarianceStatus.WARMING -> {
                when (next) {
                    TransitTimeVarianceStatus.STOPPED -> return true
                }
                return false
            }
            TransitTimeVarianceStatus.READY -> {
                when (next) {
                    TransitTimeVarianceStatus.DRAINING -> return true
                }
                return false
            }
            TransitTimeVarianceStatus.DRAINING -> {
                when (next) {
                    TransitTimeVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("transit time variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"transit time variance"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round(fuelLiters * margin * 100) / 100

    fun snapshot(): TransitTimeVariance {
        return TransitTimeVariance(
            code = code,
            title = title,
            routeCode = routeCode,
            graceDays = graceDays,
            fuelLiters = fuelLiters,
            archived = archived,
            unit = unit,
            attributes = attributes,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a transit time variance in status idle. */
fun newTransitTimeVariance(code: String, title: String): TransitTimeVariance {
    return TransitTimeVariance(
        code = code,
        title = title,
        routeCode = "",
        graceDays = 0,
        fuelLiters = 0.0,
        archived = false,
        unit = "",
        attributes = mutableMapOf<String, String>(),
        margin = 0.0,
        status = TransitTimeVarianceStatus.IDLE,
    )
}
