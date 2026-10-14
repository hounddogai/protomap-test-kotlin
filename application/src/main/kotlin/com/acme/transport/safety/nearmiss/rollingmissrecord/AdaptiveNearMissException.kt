package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive near miss exception is in its life cycle. */
enum class AdaptiveNearMissExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive near miss exception. Its code is unique within a tenant. */
data class AdaptiveNearMissException(
    var code: String,
    var name: String,
    var sequence: Int,
    var category: String,
    var capacity: Int,
    var reviewer: String,
    var loadKg: Double,
    var odometer: Int,
    var trailerId: String,
    var distanceKm: Double,
    var amount: Double,
    var status: AdaptiveNearMissExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNearMissExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNearMissExceptionStatus): Boolean {
        when (status) {
            AdaptiveNearMissExceptionStatus.NEW -> return next == AdaptiveNearMissExceptionStatus.QUEUED || next == AdaptiveNearMissExceptionStatus.FAILED
            AdaptiveNearMissExceptionStatus.QUEUED -> {
                when (next) {
                    AdaptiveNearMissExceptionStatus.DONE -> return true
                }
                return false
            }
            AdaptiveNearMissExceptionStatus.RUNNING -> return next == AdaptiveNearMissExceptionStatus.DONE || next == AdaptiveNearMissExceptionStatus.FAILED
            AdaptiveNearMissExceptionStatus.DONE -> return next == AdaptiveNearMissExceptionStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AdaptiveNearMissExceptionStatus) {
        if (!canMoveTo(next)) {
            error("adaptive near miss exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive near miss exception"} $code"

    fun weightedExposure(): Double = round((amount - odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNearMissException {
        return AdaptiveNearMissException(
            code = code,
            name = name,
            sequence = sequence,
            category = category,
            capacity = capacity,
            reviewer = reviewer,
            loadKg = loadKg,
            odometer = odometer,
            trailerId = trailerId,
            distanceKm = distanceKm,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive near miss exception in status new. */
fun newAdaptiveNearMissException(code: String, name: String): AdaptiveNearMissException {
    return AdaptiveNearMissException(
        code = code,
        name = name,
        sequence = 0,
        category = "",
        capacity = 0,
        reviewer = "",
        loadKg = 0.0,
        odometer = 0,
        trailerId = "",
        distanceKm = 0.0,
        amount = 0.0,
        status = AdaptiveNearMissExceptionStatus.NEW,
    )
}
