package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire variance is in its life cycle. */
enum class TireVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tire variance. Its code is unique within a tenant. */
data class TireVariance(
    var code: String,
    var label: String,
    var score: Double,
    var title: String,
    var fuelLiters: Double,
    var reviewer: String,
    var status: TireVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireVarianceStatus): Boolean {
        when (status) {
            TireVarianceStatus.IDLE -> return next == TireVarianceStatus.DRAINING || next == TireVarianceStatus.STOPPED
            TireVarianceStatus.WARMING -> {
                when (next) {
                    TireVarianceStatus.READY, TireVarianceStatus.DRAINING -> return true
                }
                return false
            }
            TireVarianceStatus.READY -> return next == TireVarianceStatus.DRAINING
            TireVarianceStatus.DRAINING -> return next == TireVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TireVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tire variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire variance"} $code"

    fun projectedTotal(): Double = round((fuelLiters - score) * 100) / 100

    fun snapshot(): TireVariance {
        return TireVariance(
            code = code,
            label = label,
            score = score,
            title = title,
            fuelLiters = fuelLiters,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire variance in status idle. */
fun newTireVariance(code: String, label: String): TireVariance {
    return TireVariance(
        code = code,
        label = label,
        score = 0.0,
        title = "",
        fuelLiters = 0.0,
        reviewer = "",
        status = TireVarianceStatus.IDLE,
    )
}
