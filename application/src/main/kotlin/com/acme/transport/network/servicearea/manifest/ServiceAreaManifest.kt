package com.acme.transport.network.servicearea.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The service area manifest records of network's service area work, and the rules, stores, and calculations
// around them.

/** Where a service area manifest is in its life cycle. */
enum class ServiceAreaManifestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A service area manifest as the service area team keeps it. */
data class ServiceAreaManifest(
    var code: String,
    var title: String,
    var summary: String,
    var unit: String,
    var budget: Double,
    var lane: String,
    var distanceKm: Double,
    var startsAt: Int,
    var floorValue: Double,
    var currency: String,
    var discount: Double,
    var status: ServiceAreaManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaManifestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaManifestStatus): Boolean {
        when (status) {
            ServiceAreaManifestStatus.IDLE -> {
                when (next) {
                    ServiceAreaManifestStatus.WARMING, ServiceAreaManifestStatus.DRAINING -> return true
                }
                return false
            }
            ServiceAreaManifestStatus.WARMING -> {
                when (next) {
                    ServiceAreaManifestStatus.READY -> return true
                }
                return false
            }
            ServiceAreaManifestStatus.READY -> {
                when (next) {
                    ServiceAreaManifestStatus.DRAINING, ServiceAreaManifestStatus.STOPPED -> return true
                }
                return false
            }
            ServiceAreaManifestStatus.DRAINING -> return next == ServiceAreaManifestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ServiceAreaManifestStatus) {
        if (!canMoveTo(next)) {
            error("service area manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedValue(): Double = round(discount * budget * 100) / 100

    fun snapshot(): ServiceAreaManifest {
        return ServiceAreaManifest(
            code = code,
            title = title,
            summary = summary,
            unit = unit,
            budget = budget,
            lane = lane,
            distanceKm = distanceKm,
            startsAt = startsAt,
            floorValue = floorValue,
            currency = currency,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a service area manifest in status idle. */
fun newServiceAreaManifest(code: String, title: String): ServiceAreaManifest {
    return ServiceAreaManifest(
        code = code,
        title = title,
        summary = "",
        unit = "",
        budget = 0.0,
        lane = "",
        distanceKm = 0.0,
        startsAt = 0,
        floorValue = 0.0,
        currency = "",
        discount = 0.0,
        status = ServiceAreaManifestStatus.IDLE,
    )
}
