package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area preference is in its life cycle. */
enum class ServiceAreaPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One service area preference, with what the planners need to act on it. */
data class ServiceAreaPreference(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var sequence: Int,
    var summary: String,
    var cost: Double,
    var timezone: String,
    var quantity: Int,
    var endsAt: Int,
    var category: String,
    var status: ServiceAreaPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaPreferenceStatus): Boolean {
        when (status) {
            ServiceAreaPreferenceStatus.REQUESTED -> {
                when (next) {
                    ServiceAreaPreferenceStatus.REVOKED, ServiceAreaPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            ServiceAreaPreferenceStatus.GRANTED -> return next == ServiceAreaPreferenceStatus.REVOKED
            ServiceAreaPreferenceStatus.REVOKED -> {
                when (next) {
                    ServiceAreaPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("service area preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedExposure(): Double = round((cost - quantity.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaPreference {
        return ServiceAreaPreference(
            code = code,
            label = label,
            pinned = pinned,
            sequence = sequence,
            summary = summary,
            cost = cost,
            timezone = timezone,
            quantity = quantity,
            endsAt = endsAt,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a service area preference in status requested. */
fun newServiceAreaPreference(code: String, label: String): ServiceAreaPreference {
    return ServiceAreaPreference(
        code = code,
        label = label,
        pinned = false,
        sequence = 0,
        summary = "",
        cost = 0.0,
        timezone = "",
        quantity = 0,
        endsAt = 0,
        category = "",
        status = ServiceAreaPreferenceStatus.REQUESTED,
    )
}
