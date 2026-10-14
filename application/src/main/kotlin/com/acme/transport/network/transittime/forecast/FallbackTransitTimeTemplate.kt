package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback transit time template is in its life cycle. */
enum class FallbackTransitTimeTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback transit time template as the transit time team keeps it. */
data class FallbackTransitTimeTemplate(
    var code: String,
    var name: String,
    var channel: String,
    var limit: Int,
    var fuelLiters: Double,
    var version: Int,
    var capacity: Int,
    var status: FallbackTransitTimeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTransitTimeTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTransitTimeTemplateStatus): Boolean {
        when (status) {
            FallbackTransitTimeTemplateStatus.PLANNED -> {
                when (next) {
                    FallbackTransitTimeTemplateStatus.IN_PROGRESS -> return true
                }
                return false
            }
            FallbackTransitTimeTemplateStatus.SCHEDULED -> return next == FallbackTransitTimeTemplateStatus.IN_PROGRESS || next == FallbackTransitTimeTemplateStatus.CANCELLED
            FallbackTransitTimeTemplateStatus.IN_PROGRESS -> return next == FallbackTransitTimeTemplateStatus.CANCELLED
            FallbackTransitTimeTemplateStatus.COMPLETED -> return next == FallbackTransitTimeTemplateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackTransitTimeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("fallback transit time template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round((version.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): FallbackTransitTimeTemplate {
        return FallbackTransitTimeTemplate(
            code = code,
            name = name,
            channel = channel,
            limit = limit,
            fuelLiters = fuelLiters,
            version = version,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback transit time template in status planned. */
fun newFallbackTransitTimeTemplate(code: String, name: String): FallbackTransitTimeTemplate {
    return FallbackTransitTimeTemplate(
        code = code,
        name = name,
        channel = "",
        limit = 0,
        fuelLiters = 0.0,
        version = 0,
        capacity = 0,
        status = FallbackTransitTimeTemplateStatus.PLANNED,
    )
}
