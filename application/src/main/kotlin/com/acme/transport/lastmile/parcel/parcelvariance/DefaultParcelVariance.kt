package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default parcel variance records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a default parcel variance is in its life cycle. */
enum class DefaultParcelVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A default parcel variance as the parcel team keeps it. */
data class DefaultParcelVariance(
    var code: String,
    var name: String,
    var score: Double,
    var segment: String,
    var visible: Boolean,
    var distanceKm: Double,
    var ceiling: Double,
    var externalRef: String,
    var fuelLiters: Double,
    var status: DefaultParcelVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelVarianceStatus): Boolean {
        when (status) {
            DefaultParcelVarianceStatus.DRAFT -> {
                when (next) {
                    DefaultParcelVarianceStatus.CLOSED -> return true
                }
                return false
            }
            DefaultParcelVarianceStatus.PENDING -> return next == DefaultParcelVarianceStatus.SUSPENDED || next == DefaultParcelVarianceStatus.CLOSED
            DefaultParcelVarianceStatus.ACTIVE -> return next == DefaultParcelVarianceStatus.CLOSED
            DefaultParcelVarianceStatus.SUSPENDED -> return next == DefaultParcelVarianceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DefaultParcelVarianceStatus) {
        if (!canMoveTo(next)) {
            error("default parcel variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default parcel variance"} $code"

    fun projectedExposure(): Double = round((score - distanceKm) * 100) / 100

    fun snapshot(): DefaultParcelVariance {
        return DefaultParcelVariance(
            code = code,
            name = name,
            score = score,
            segment = segment,
            visible = visible,
            distanceKm = distanceKm,
            ceiling = ceiling,
            externalRef = externalRef,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default parcel variance in status draft. */
fun newDefaultParcelVariance(code: String, name: String): DefaultParcelVariance {
    return DefaultParcelVariance(
        code = code,
        name = name,
        score = 0.0,
        segment = "",
        visible = false,
        distanceKm = 0.0,
        ceiling = 0.0,
        externalRef = "",
        fuelLiters = 0.0,
        status = DefaultParcelVarianceStatus.DRAFT,
    )
}
