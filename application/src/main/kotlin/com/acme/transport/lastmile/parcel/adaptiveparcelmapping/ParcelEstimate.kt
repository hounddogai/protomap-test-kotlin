package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel estimate is in its life cycle. */
enum class ParcelEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a parcel estimate and its current status. */
data class ParcelEstimate(
    var code: String,
    var label: String,
    var budget: Double,
    var maximum: Double,
    var revision: Int,
    var startsAt: Int,
    var region: String,
    var fuelLiters: Double,
    var floorValue: Double,
    var status: ParcelEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelEstimateStatus): Boolean {
        when (status) {
            ParcelEstimateStatus.NEW -> {
                when (next) {
                    ParcelEstimateStatus.QUEUED -> return true
                }
                return false
            }
            ParcelEstimateStatus.QUEUED -> return next == ParcelEstimateStatus.FAILED
            ParcelEstimateStatus.RUNNING -> {
                when (next) {
                    ParcelEstimateStatus.DONE -> return true
                }
                return false
            }
            ParcelEstimateStatus.DONE -> return next == ParcelEstimateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ParcelEstimateStatus) {
        if (!canMoveTo(next)) {
            error("parcel estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun netLoad(): Double = round(fuelLiters * floorValue * 100) / 100

    fun snapshot(): ParcelEstimate {
        return ParcelEstimate(
            code = code,
            label = label,
            budget = budget,
            maximum = maximum,
            revision = revision,
            startsAt = startsAt,
            region = region,
            fuelLiters = fuelLiters,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a parcel estimate in status new. */
fun newParcelEstimate(code: String, label: String): ParcelEstimate {
    return ParcelEstimate(
        code = code,
        label = label,
        budget = 0.0,
        maximum = 0.0,
        revision = 0,
        startsAt = 0,
        region = "",
        fuelLiters = 0.0,
        floorValue = 0.0,
        status = ParcelEstimateStatus.NEW,
    )
}
