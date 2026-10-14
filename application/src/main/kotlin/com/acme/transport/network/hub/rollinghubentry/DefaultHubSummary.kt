package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default hub summary is in its life cycle. */
enum class DefaultHubSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default hub summary as the hub team keeps it. */
data class DefaultHubSummary(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var active: Boolean,
    var distanceKm: Double,
    var odometer: Int,
    var category: String,
    var phone: String,
    var status: DefaultHubSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultHubSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultHubSummaryStatus): Boolean {
        when (status) {
            DefaultHubSummaryStatus.CREATED -> {
                when (next) {
                    DefaultHubSummaryStatus.VALIDATED, DefaultHubSummaryStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultHubSummaryStatus.VALIDATED -> return next == DefaultHubSummaryStatus.RETIRED
            DefaultHubSummaryStatus.PUBLISHED -> {
                when (next) {
                    DefaultHubSummaryStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultHubSummaryStatus) {
        if (!canMoveTo(next)) {
            error("default hub summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((distanceKm + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultHubSummary {
        return DefaultHubSummary(
            code = code,
            name = name,
            expiresAt = expiresAt,
            active = active,
            distanceKm = distanceKm,
            odometer = odometer,
            category = category,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default hub summary in status created. */
fun newDefaultHubSummary(code: String, name: String): DefaultHubSummary {
    return DefaultHubSummary(
        code = code,
        name = name,
        expiresAt = 0,
        active = false,
        distanceKm = 0.0,
        odometer = 0,
        category = "",
        phone = "",
        status = DefaultHubSummaryStatus.CREATED,
    )
}
