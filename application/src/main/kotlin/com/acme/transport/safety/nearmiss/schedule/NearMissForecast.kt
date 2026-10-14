package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss forecast is in its life cycle. */
enum class NearMissForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A near miss forecast. Its code is unique within a tenant. */
data class NearMissForecast(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var discount: Double,
    var driverId: String,
    var priority: Int,
    var retries: Int,
    var margin: Double,
    var status: NearMissForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissForecastStatus): Boolean {
        when (status) {
            NearMissForecastStatus.NEW -> {
                when (next) {
                    NearMissForecastStatus.DONE -> return true
                }
                return false
            }
            NearMissForecastStatus.QUEUED -> {
                when (next) {
                    NearMissForecastStatus.DONE -> return true
                }
                return false
            }
            NearMissForecastStatus.RUNNING -> {
                when (next) {
                    NearMissForecastStatus.DONE, NearMissForecastStatus.FAILED -> return true
                }
                return false
            }
            NearMissForecastStatus.DONE -> {
                when (next) {
                    NearMissForecastStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissForecastStatus) {
        if (!canMoveTo(next)) {
            error("near miss forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"near miss forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((discount + quantity.toDouble()) * 100) / 100

    fun snapshot(): NearMissForecast {
        return NearMissForecast(
            code = code,
            title = title,
            attributes = attributes,
            quantity = quantity,
            discount = discount,
            driverId = driverId,
            priority = priority,
            retries = retries,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a near miss forecast in status new. */
fun newNearMissForecast(code: String, title: String): NearMissForecast {
    return NearMissForecast(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        discount = 0.0,
        driverId = "",
        priority = 0,
        retries = 0,
        margin = 0.0,
        status = NearMissForecastStatus.NEW,
    )
}
