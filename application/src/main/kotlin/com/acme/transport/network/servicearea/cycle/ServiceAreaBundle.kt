package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area bundle is in its life cycle. */
enum class ServiceAreaBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One service area bundle, with what the planners need to act on it. */
data class ServiceAreaBundle(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var capacity: Int,
    var externalRef: String,
    var segment: String,
    var region: String,
    var pinned: Boolean,
    var ratio: Double,
    var status: ServiceAreaBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaBundleStatus): Boolean {
        when (status) {
            ServiceAreaBundleStatus.IDLE -> {
                when (next) {
                    ServiceAreaBundleStatus.WARMING, ServiceAreaBundleStatus.DRAINING -> return true
                }
                return false
            }
            ServiceAreaBundleStatus.WARMING -> return next == ServiceAreaBundleStatus.DRAINING || next == ServiceAreaBundleStatus.STOPPED
            ServiceAreaBundleStatus.READY -> {
                when (next) {
                    ServiceAreaBundleStatus.DRAINING -> return true
                }
                return false
            }
            ServiceAreaBundleStatus.DRAINING -> {
                when (next) {
                    ServiceAreaBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ServiceAreaBundleStatus) {
        if (!canMoveTo(next)) {
            error("service area bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round(ratio * fuelLiters * 100) / 100

    fun snapshot(): ServiceAreaBundle {
        return ServiceAreaBundle(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            capacity = capacity,
            externalRef = externalRef,
            segment = segment,
            region = region,
            pinned = pinned,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a service area bundle in status idle. */
fun newServiceAreaBundle(code: String, title: String): ServiceAreaBundle {
    return ServiceAreaBundle(
        code = code,
        title = title,
        fuelLiters = 0.0,
        capacity = 0,
        externalRef = "",
        segment = "",
        region = "",
        pinned = false,
        ratio = 0.0,
        status = ServiceAreaBundleStatus.IDLE,
    )
}
