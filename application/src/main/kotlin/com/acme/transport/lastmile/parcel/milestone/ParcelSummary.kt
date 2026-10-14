package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel summary is in its life cycle. */
enum class ParcelSummaryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A parcel summary. Its code is unique within a tenant. */
data class ParcelSummary(
    var code: String,
    var title: String,
    var amount: Double,
    var loadKg: Double,
    var lane: String,
    var revision: Int,
    var maximum: Double,
    var distanceKm: Double,
    var shiftCode: String,
    var odometer: Int,
    var status: ParcelSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelSummaryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelSummaryStatus): Boolean {
        when (status) {
            ParcelSummaryStatus.REQUESTED -> return next == ParcelSummaryStatus.GRANTED
            ParcelSummaryStatus.GRANTED -> {
                when (next) {
                    ParcelSummaryStatus.EXPIRED -> return true
                }
                return false
            }
            ParcelSummaryStatus.REVOKED -> {
                when (next) {
                    ParcelSummaryStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelSummaryStatus) {
        if (!canMoveTo(next)) {
            error("parcel summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"parcel summary"} $code"

    fun projectedLoad(): Double = round((maximum + revision.toDouble()) * 100) / 100

    fun snapshot(): ParcelSummary {
        return ParcelSummary(
            code = code,
            title = title,
            amount = amount,
            loadKg = loadKg,
            lane = lane,
            revision = revision,
            maximum = maximum,
            distanceKm = distanceKm,
            shiftCode = shiftCode,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a parcel summary in status requested. */
fun newParcelSummary(code: String, title: String): ParcelSummary {
    return ParcelSummary(
        code = code,
        title = title,
        amount = 0.0,
        loadKg = 0.0,
        lane = "",
        revision = 0,
        maximum = 0.0,
        distanceKm = 0.0,
        shiftCode = "",
        odometer = 0,
        status = ParcelSummaryStatus.REQUESTED,
    )
}
