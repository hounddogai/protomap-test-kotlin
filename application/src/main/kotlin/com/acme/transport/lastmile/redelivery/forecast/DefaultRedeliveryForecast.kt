package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default redelivery forecast records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a default redelivery forecast is in its life cycle. */
enum class DefaultRedeliveryForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default redelivery forecast as the redelivery team keeps it. */
data class DefaultRedeliveryForecast(
    var code: String,
    var title: String,
    var archived: Boolean,
    var lane: String,
    var active: Boolean,
    var updatedAt: Int,
    var pallets: Int,
    var priority: Int,
    var carrier: String,
    var amount: Double,
    var status: DefaultRedeliveryForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRedeliveryForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRedeliveryForecastStatus): Boolean {
        when (status) {
            DefaultRedeliveryForecastStatus.DRAFT -> return next == DefaultRedeliveryForecastStatus.PENDING || next == DefaultRedeliveryForecastStatus.CLOSED
            DefaultRedeliveryForecastStatus.PENDING -> {
                when (next) {
                    DefaultRedeliveryForecastStatus.CLOSED -> return true
                }
                return false
            }
            DefaultRedeliveryForecastStatus.ACTIVE -> {
                when (next) {
                    DefaultRedeliveryForecastStatus.SUSPENDED, DefaultRedeliveryForecastStatus.CLOSED -> return true
                }
                return false
            }
            DefaultRedeliveryForecastStatus.SUSPENDED -> {
                when (next) {
                    DefaultRedeliveryForecastStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRedeliveryForecastStatus) {
        if (!canMoveTo(next)) {
            error("default redelivery forecast $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default redelivery forecast"} $code"

    fun projectedLoad(): Double = round(priority.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): DefaultRedeliveryForecast {
        return DefaultRedeliveryForecast(
            code = code,
            title = title,
            archived = archived,
            lane = lane,
            active = active,
            updatedAt = updatedAt,
            pallets = pallets,
            priority = priority,
            carrier = carrier,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default redelivery forecast in status draft. */
fun newDefaultRedeliveryForecast(code: String, title: String): DefaultRedeliveryForecast {
    return DefaultRedeliveryForecast(
        code = code,
        title = title,
        archived = false,
        lane = "",
        active = false,
        updatedAt = 0,
        pallets = 0,
        priority = 0,
        carrier = "",
        amount = 0.0,
        status = DefaultRedeliveryForecastStatus.DRAFT,
    )
}
