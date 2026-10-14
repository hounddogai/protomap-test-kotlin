package com.acme.transport.fleetops.leasereturn.returnforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global lease return forecast records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a global lease return forecast is in its life cycle. */
enum class GlobalLeaseReturnForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global lease return forecast as the lease return team keeps it. */
data class GlobalLeaseReturnForecast(
    var code: String,
    var label: String,
    var retries: Int,
    var contactEmail: String,
    var margin: Double,
    var trailerId: String,
    var lane: String,
    var phone: String,
    var endsAt: Int,
    var dock: String,
    var status: GlobalLeaseReturnForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLeaseReturnForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLeaseReturnForecastStatus): Boolean {
        when (status) {
            GlobalLeaseReturnForecastStatus.DRAFT -> {
                when (next) {
                    GlobalLeaseReturnForecastStatus.ACTIVE -> return true
                }
                return false
            }
            GlobalLeaseReturnForecastStatus.PENDING -> {
                when (next) {
                    GlobalLeaseReturnForecastStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalLeaseReturnForecastStatus.ACTIVE -> {
                when (next) {
                    GlobalLeaseReturnForecastStatus.CLOSED -> return true
                }
                return false
            }
            GlobalLeaseReturnForecastStatus.SUSPENDED -> {
                when (next) {
                    GlobalLeaseReturnForecastStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalLeaseReturnForecastStatus) {
        if (!canMoveTo(next)) {
            error("global lease return forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((retries.toDouble() - margin) * 100) / 100

    fun snapshot(): GlobalLeaseReturnForecast {
        return GlobalLeaseReturnForecast(
            code = code,
            label = label,
            retries = retries,
            contactEmail = contactEmail,
            margin = margin,
            trailerId = trailerId,
            lane = lane,
            phone = phone,
            endsAt = endsAt,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global lease return forecast in status draft. */
fun newGlobalLeaseReturnForecast(code: String, label: String): GlobalLeaseReturnForecast {
    return GlobalLeaseReturnForecast(
        code = code,
        label = label,
        retries = 0,
        contactEmail = "",
        margin = 0.0,
        trailerId = "",
        lane = "",
        phone = "",
        endsAt = 0,
        dock = "",
        status = GlobalLeaseReturnForecastStatus.DRAFT,
    )
}
