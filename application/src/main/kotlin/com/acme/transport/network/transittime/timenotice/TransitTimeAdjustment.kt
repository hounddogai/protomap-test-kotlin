package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time adjustment is in its life cycle. */
enum class TransitTimeAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One transit time adjustment, with what the planners need to act on it. */
data class TransitTimeAdjustment(
    var code: String,
    var label: String,
    var limit: Int,
    var trailerId: String,
    var distanceKm: Double,
    var maximum: Double,
    var status: TransitTimeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeAdjustmentStatus): Boolean {
        when (status) {
            TransitTimeAdjustmentStatus.NEW -> return next == TransitTimeAdjustmentStatus.QUEUED || next == TransitTimeAdjustmentStatus.RUNNING
            TransitTimeAdjustmentStatus.QUEUED -> return next == TransitTimeAdjustmentStatus.FAILED
            TransitTimeAdjustmentStatus.RUNNING -> {
                when (next) {
                    TransitTimeAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            TransitTimeAdjustmentStatus.DONE -> {
                when (next) {
                    TransitTimeAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("transit time adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"transit time adjustment"} $code"

    fun projectedExposure(): Double = round((limit.toDouble() - maximum) * 100) / 100

    fun snapshot(): TransitTimeAdjustment {
        return TransitTimeAdjustment(
            code = code,
            label = label,
            limit = limit,
            trailerId = trailerId,
            distanceKm = distanceKm,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a transit time adjustment in status new. */
fun newTransitTimeAdjustment(code: String, label: String): TransitTimeAdjustment {
    return TransitTimeAdjustment(
        code = code,
        label = label,
        limit = 0,
        trailerId = "",
        distanceKm = 0.0,
        maximum = 0.0,
        status = TransitTimeAdjustmentStatus.NEW,
    )
}
