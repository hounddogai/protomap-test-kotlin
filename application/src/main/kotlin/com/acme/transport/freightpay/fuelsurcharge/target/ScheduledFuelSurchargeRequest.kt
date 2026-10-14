package com.acme.transport.freightpay.fuelsurcharge.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled fuel surcharge request is in its life cycle. */
enum class ScheduledFuelSurchargeRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled fuel surcharge request as the fuel surcharge team keeps it. */
data class ScheduledFuelSurchargeRequest(
    var code: String,
    var title: String,
    var version: Int,
    var category: String,
    var distanceKm: Double,
    var timezone: String,
    var status: ScheduledFuelSurchargeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelSurchargeRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelSurchargeRequestStatus): Boolean {
        when (status) {
            ScheduledFuelSurchargeRequestStatus.PLANNED -> {
                when (next) {
                    ScheduledFuelSurchargeRequestStatus.IN_PROGRESS, ScheduledFuelSurchargeRequestStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledFuelSurchargeRequestStatus.SCHEDULED -> return next == ScheduledFuelSurchargeRequestStatus.IN_PROGRESS
            ScheduledFuelSurchargeRequestStatus.IN_PROGRESS -> return next == ScheduledFuelSurchargeRequestStatus.COMPLETED || next == ScheduledFuelSurchargeRequestStatus.CANCELLED
            ScheduledFuelSurchargeRequestStatus.COMPLETED -> return next == ScheduledFuelSurchargeRequestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledFuelSurchargeRequestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel surcharge request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round((distanceKm + version.toDouble()) * 100) / 100

    fun snapshot(): ScheduledFuelSurchargeRequest {
        return ScheduledFuelSurchargeRequest(
            code = code,
            title = title,
            version = version,
            category = category,
            distanceKm = distanceKm,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled fuel surcharge request in status planned. */
fun newScheduledFuelSurchargeRequest(code: String, title: String): ScheduledFuelSurchargeRequest {
    return ScheduledFuelSurchargeRequest(
        code = code,
        title = title,
        version = 0,
        category = "",
        distanceKm = 0.0,
        timezone = "",
        status = ScheduledFuelSurchargeRequestStatus.PLANNED,
    )
}
