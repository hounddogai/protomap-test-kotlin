package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional parcel variance is in its life cycle. */
enum class RegionalParcelVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional parcel variance as the parcel team keeps it. */
data class RegionalParcelVariance(
    var code: String,
    var name: String,
    var threshold: Double,
    var minimum: Double,
    var timezone: String,
    var depot: String,
    var contactEmail: String,
    var capacity: Int,
    var status: RegionalParcelVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelVarianceStatus): Boolean {
        when (status) {
            RegionalParcelVarianceStatus.PLANNED -> return next == RegionalParcelVarianceStatus.SCHEDULED
            RegionalParcelVarianceStatus.SCHEDULED -> {
                when (next) {
                    RegionalParcelVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            RegionalParcelVarianceStatus.IN_PROGRESS -> return next == RegionalParcelVarianceStatus.COMPLETED || next == RegionalParcelVarianceStatus.CANCELLED
            RegionalParcelVarianceStatus.COMPLETED -> return next == RegionalParcelVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalParcelVarianceStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round(capacity.toDouble() * minimum * 100) / 100

    fun snapshot(): RegionalParcelVariance {
        return RegionalParcelVariance(
            code = code,
            name = name,
            threshold = threshold,
            minimum = minimum,
            timezone = timezone,
            depot = depot,
            contactEmail = contactEmail,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional parcel variance in status planned. */
fun newRegionalParcelVariance(code: String, name: String): RegionalParcelVariance {
    return RegionalParcelVariance(
        code = code,
        name = name,
        threshold = 0.0,
        minimum = 0.0,
        timezone = "",
        depot = "",
        contactEmail = "",
        capacity = 0,
        status = RegionalParcelVarianceStatus.PLANNED,
    )
}
