package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a service area score is in its life cycle. */
enum class ServiceAreaScoreStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A service area score as the service area team keeps it. */
data class ServiceAreaScore(
    var code: String,
    var label: String,
    var lane: String,
    var batchSize: Int,
    var retries: Int,
    var capacity: Int,
    var durationMinutes: Int,
    var rate: Double,
    var discount: Double,
    var attempts: Int,
    var vehicleId: String,
    var status: ServiceAreaScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ServiceAreaScoreStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ServiceAreaScoreStatus): Boolean {
        when (status) {
            ServiceAreaScoreStatus.REQUESTED -> {
                when (next) {
                    ServiceAreaScoreStatus.EXPIRED -> return true
                }
                return false
            }
            ServiceAreaScoreStatus.GRANTED -> {
                when (next) {
                    ServiceAreaScoreStatus.REVOKED -> return true
                }
                return false
            }
            ServiceAreaScoreStatus.REVOKED -> return next == ServiceAreaScoreStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ServiceAreaScoreStatus) {
        if (!canMoveTo(next)) {
            error("service area score $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"service area score"} $code"

    fun netExposure(): Double = round((rate + capacity.toDouble()) * 100) / 100

    fun snapshot(): ServiceAreaScore {
        return ServiceAreaScore(
            code = code,
            label = label,
            lane = lane,
            batchSize = batchSize,
            retries = retries,
            capacity = capacity,
            durationMinutes = durationMinutes,
            rate = rate,
            discount = discount,
            attempts = attempts,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a service area score in status requested. */
fun newServiceAreaScore(code: String, label: String): ServiceAreaScore {
    return ServiceAreaScore(
        code = code,
        label = label,
        lane = "",
        batchSize = 0,
        retries = 0,
        capacity = 0,
        durationMinutes = 0,
        rate = 0.0,
        discount = 0.0,
        attempts = 0,
        vehicleId = "",
        status = ServiceAreaScoreStatus.REQUESTED,
    )
}
