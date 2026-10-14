package com.acme.transport.lastmile.parcel.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal parcel quota records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a seasonal parcel quota is in its life cycle. */
enum class SeasonalParcelQuotaStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal parcel quota and its current status. */
data class SeasonalParcelQuota(
    var code: String,
    var title: String,
    var trailerId: String,
    var checksum: String,
    var discount: Double,
    var active: Boolean,
    var distanceKm: Double,
    var capacity: Int,
    var shiftCode: String,
    var country: String,
    var status: SeasonalParcelQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalParcelQuotaStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalParcelQuotaStatus): Boolean {
        when (status) {
            SeasonalParcelQuotaStatus.REQUESTED -> return next == SeasonalParcelQuotaStatus.GRANTED || next == SeasonalParcelQuotaStatus.EXPIRED
            SeasonalParcelQuotaStatus.GRANTED -> {
                when (next) {
                    SeasonalParcelQuotaStatus.EXPIRED -> return true
                }
                return false
            }
            SeasonalParcelQuotaStatus.REVOKED -> {
                when (next) {
                    SeasonalParcelQuotaStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalParcelQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal parcel quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((distanceKm + capacity.toDouble()) * 100) / 100

    fun snapshot(): SeasonalParcelQuota {
        return SeasonalParcelQuota(
            code = code,
            title = title,
            trailerId = trailerId,
            checksum = checksum,
            discount = discount,
            active = active,
            distanceKm = distanceKm,
            capacity = capacity,
            shiftCode = shiftCode,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal parcel quota in status requested. */
fun newSeasonalParcelQuota(code: String, title: String): SeasonalParcelQuota {
    return SeasonalParcelQuota(
        code = code,
        title = title,
        trailerId = "",
        checksum = "",
        discount = 0.0,
        active = false,
        distanceKm = 0.0,
        capacity = 0,
        shiftCode = "",
        country = "",
        status = SeasonalParcelQuotaStatus.REQUESTED,
    )
}
