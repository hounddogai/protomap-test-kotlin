package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area segment is in its life cycle. */
enum class ServiceAreaSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A service area segment as the service area team keeps it. */
data class ServiceAreaSegment(
    var code: String,
    var name: String,
    var threshold: Double,
    var country: String,
    var margin: Double,
    var carrier: String,
    var timezone: String,
    var visible: Boolean,
    var batchSize: Int,
    var status: ServiceAreaSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaSegmentStatus): Boolean {
        when (status) {
            ServiceAreaSegmentStatus.PROPOSED -> {
                when (next) {
                    ServiceAreaSegmentStatus.APPROVED -> return true
                }
                return false
            }
            ServiceAreaSegmentStatus.APPROVED -> {
                when (next) {
                    ServiceAreaSegmentStatus.REJECTED, ServiceAreaSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            ServiceAreaSegmentStatus.REJECTED -> return next == ServiceAreaSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ServiceAreaSegmentStatus) {
        if (!canMoveTo(next)) {
            error("service area segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round((margin + threshold) * 100) / 100

    fun snapshot(): ServiceAreaSegment {
        return ServiceAreaSegment(
            code = code,
            name = name,
            threshold = threshold,
            country = country,
            margin = margin,
            carrier = carrier,
            timezone = timezone,
            visible = visible,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a service area segment in status proposed. */
fun newServiceAreaSegment(code: String, name: String): ServiceAreaSegment {
    return ServiceAreaSegment(
        code = code,
        name = name,
        threshold = 0.0,
        country = "",
        margin = 0.0,
        carrier = "",
        timezone = "",
        visible = false,
        batchSize = 0,
        status = ServiceAreaSegmentStatus.PROPOSED,
    )
}
