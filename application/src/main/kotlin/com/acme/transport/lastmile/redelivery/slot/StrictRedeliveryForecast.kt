package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.text.normalize

/** Where a strict redelivery forecast is in its life cycle. */
enum class StrictRedeliveryForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict redelivery forecast as the redelivery team keeps it. */
data class StrictRedeliveryForecast(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var locale: String,
    var routeCode: String,
    var reviewer: String,
    var quantity: Int,
    var status: StrictRedeliveryForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRedeliveryForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRedeliveryForecastStatus): Boolean {
        when (status) {
            StrictRedeliveryForecastStatus.OPEN -> {
                when (next) {
                    StrictRedeliveryForecastStatus.IN_REVIEW, StrictRedeliveryForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictRedeliveryForecastStatus.IN_REVIEW -> {
                when (next) {
                    StrictRedeliveryForecastStatus.RESOLVED -> return true
                }
                return false
            }
            StrictRedeliveryForecastStatus.RESOLVED -> {
                when (next) {
                    StrictRedeliveryForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictRedeliveryForecastStatus.REOPENED -> {
                when (next) {
                    StrictRedeliveryForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRedeliveryForecastStatus) {
        if (!canMoveTo(next)) {
            error("strict redelivery forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): StrictRedeliveryForecast {
        return StrictRedeliveryForecast(
            code = code,
            label = label,
            tags = tags,
            locale = locale,
            routeCode = routeCode,
            reviewer = reviewer,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict redelivery forecast in status open. */
fun newStrictRedeliveryForecast(code: String, label: String): StrictRedeliveryForecast {
    return StrictRedeliveryForecast(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        locale = "",
        routeCode = "",
        reviewer = "",
        quantity = 0,
        status = StrictRedeliveryForecastStatus.OPEN,
    )
}
