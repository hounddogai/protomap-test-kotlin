package com.acme.transport.lastmile.proofofdelivery.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery forecast is in its life cycle. */
enum class ProofOfDeliveryForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a proof of delivery forecast and its current status. */
data class ProofOfDeliveryForecast(
    var code: String,
    var title: String,
    var stopCount: Int,
    var endsAt: Int,
    var vehicleId: String,
    var threshold: Double,
    var description: String,
    var status: ProofOfDeliveryForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryForecastStatus): Boolean {
        when (status) {
            ProofOfDeliveryForecastStatus.NEW -> {
                when (next) {
                    ProofOfDeliveryForecastStatus.FAILED -> return true
                }
                return false
            }
            ProofOfDeliveryForecastStatus.QUEUED -> return next == ProofOfDeliveryForecastStatus.RUNNING || next == ProofOfDeliveryForecastStatus.FAILED
            ProofOfDeliveryForecastStatus.RUNNING -> return next == ProofOfDeliveryForecastStatus.FAILED
            ProofOfDeliveryForecastStatus.DONE -> return next == ProofOfDeliveryForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryForecastStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round(threshold * endsAt.toDouble() * 100) / 100

    fun snapshot(): ProofOfDeliveryForecast {
        return ProofOfDeliveryForecast(
            code = code,
            title = title,
            stopCount = stopCount,
            endsAt = endsAt,
            vehicleId = vehicleId,
            threshold = threshold,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a proof of delivery forecast in status new. */
fun newProofOfDeliveryForecast(code: String, title: String): ProofOfDeliveryForecast {
    return ProofOfDeliveryForecast(
        code = code,
        title = title,
        stopCount = 0,
        endsAt = 0,
        vehicleId = "",
        threshold = 0.0,
        description = "",
        status = ProofOfDeliveryForecastStatus.NEW,
    )
}
