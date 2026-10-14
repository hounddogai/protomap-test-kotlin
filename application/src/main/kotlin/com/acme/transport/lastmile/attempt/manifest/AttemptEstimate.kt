package com.acme.transport.lastmile.attempt.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt estimate is in its life cycle. */
enum class AttemptEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A attempt estimate. Its code is unique within a tenant. */
data class AttemptEstimate(
    var code: String,
    var name: String,
    var lane: String,
    var depot: String,
    var graceDays: Int,
    var revision: Int,
    var windowDays: Int,
    var floorValue: Double,
    var odometer: Int,
    var attempts: Int,
    var status: AttemptEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptEstimateStatus): Boolean {
        when (status) {
            AttemptEstimateStatus.NEW -> {
                when (next) {
                    AttemptEstimateStatus.RUNNING, AttemptEstimateStatus.FAILED -> return true
                }
                return false
            }
            AttemptEstimateStatus.QUEUED -> {
                when (next) {
                    AttemptEstimateStatus.RUNNING, AttemptEstimateStatus.FAILED -> return true
                }
                return false
            }
            AttemptEstimateStatus.RUNNING -> {
                when (next) {
                    AttemptEstimateStatus.DONE -> return true
                }
                return false
            }
            AttemptEstimateStatus.DONE -> {
                when (next) {
                    AttemptEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AttemptEstimateStatus) {
        if (!canMoveTo(next)) {
            error("attempt estimate $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"attempt estimate"} $code"

    fun adjustedExposure(): Double = round((revision.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): AttemptEstimate {
        return AttemptEstimate(
            code = code,
            name = name,
            lane = lane,
            depot = depot,
            graceDays = graceDays,
            revision = revision,
            windowDays = windowDays,
            floorValue = floorValue,
            odometer = odometer,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a attempt estimate in status new. */
fun newAttemptEstimate(code: String, name: String): AttemptEstimate {
    return AttemptEstimate(
        code = code,
        name = name,
        lane = "",
        depot = "",
        graceDays = 0,
        revision = 0,
        windowDays = 0,
        floorValue = 0.0,
        odometer = 0,
        attempts = 0,
        status = AttemptEstimateStatus.NEW,
    )
}
