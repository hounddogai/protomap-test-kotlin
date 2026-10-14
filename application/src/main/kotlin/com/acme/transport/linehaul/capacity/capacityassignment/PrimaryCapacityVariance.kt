package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary capacity variance is in its life cycle. */
enum class PrimaryCapacityVarianceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One primary capacity variance, with what the planners need to act on it. */
data class PrimaryCapacityVariance(
    var code: String,
    var title: String,
    var summary: String,
    var expiresAt: Int,
    var odometer: Int,
    var source: String,
    var carrier: String,
    var status: PrimaryCapacityVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryCapacityVarianceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryCapacityVarianceStatus): Boolean {
        when (status) {
            PrimaryCapacityVarianceStatus.REQUESTED -> {
                when (next) {
                    PrimaryCapacityVarianceStatus.GRANTED -> return true
                }
                return false
            }
            PrimaryCapacityVarianceStatus.GRANTED -> {
                when (next) {
                    PrimaryCapacityVarianceStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryCapacityVarianceStatus.REVOKED -> {
                when (next) {
                    PrimaryCapacityVarianceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryCapacityVarianceStatus) {
        if (!canMoveTo(next)) {
            error("primary capacity variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round((odometer.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryCapacityVariance {
        return PrimaryCapacityVariance(
            code = code,
            title = title,
            summary = summary,
            expiresAt = expiresAt,
            odometer = odometer,
            source = source,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary capacity variance in status requested. */
fun newPrimaryCapacityVariance(code: String, title: String): PrimaryCapacityVariance {
    return PrimaryCapacityVariance(
        code = code,
        title = title,
        summary = "",
        expiresAt = 0,
        odometer = 0,
        source = "",
        carrier = "",
        status = PrimaryCapacityVarianceStatus.REQUESTED,
    )
}
