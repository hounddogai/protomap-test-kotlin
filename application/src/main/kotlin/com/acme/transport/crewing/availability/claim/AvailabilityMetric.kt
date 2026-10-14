package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability metric is in its life cycle. */
enum class AvailabilityMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One availability metric, with what the planners need to act on it. */
data class AvailabilityMetric(
    var code: String,
    var title: String,
    var cost: Double,
    var retries: Int,
    var distanceKm: Double,
    var contactEmail: String,
    var source: String,
    var status: AvailabilityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityMetricStatus): Boolean {
        when (status) {
            AvailabilityMetricStatus.REQUESTED -> return next == AvailabilityMetricStatus.GRANTED || next == AvailabilityMetricStatus.EXPIRED
            AvailabilityMetricStatus.GRANTED -> {
                when (next) {
                    AvailabilityMetricStatus.EXPIRED -> return true
                }
                return false
            }
            AvailabilityMetricStatus.REVOKED -> {
                when (next) {
                    AvailabilityMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityMetricStatus) {
        if (!canMoveTo(next)) {
            error("availability metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"availability metric"} $code"

    fun adjustedLoad(): Double = round((distanceKm - retries.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityMetric {
        return AvailabilityMetric(
            code = code,
            title = title,
            cost = cost,
            retries = retries,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a availability metric in status requested. */
fun newAvailabilityMetric(code: String, title: String): AvailabilityMetric {
    return AvailabilityMetric(
        code = code,
        title = title,
        cost = 0.0,
        retries = 0,
        distanceKm = 0.0,
        contactEmail = "",
        source = "",
        status = AvailabilityMetricStatus.REQUESTED,
    )
}
