package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered transit time variance is in its life cycle. */
enum class TieredTransitTimeVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tiered transit time variance. Its code is unique within a tenant. */
data class TieredTransitTimeVariance(
    var code: String,
    var name: String,
    var stopCount: Int,
    var lane: String,
    var minimum: Double,
    var shiftCode: String,
    var budget: Double,
    var durationMinutes: Int,
    var source: String,
    var priority: Int,
    var status: TieredTransitTimeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTransitTimeVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTransitTimeVarianceStatus): Boolean {
        when (status) {
            TieredTransitTimeVarianceStatus.NEW -> {
                when (next) {
                    TieredTransitTimeVarianceStatus.QUEUED, TieredTransitTimeVarianceStatus.FAILED -> return true
                }
                return false
            }
            TieredTransitTimeVarianceStatus.QUEUED -> {
                when (next) {
                    TieredTransitTimeVarianceStatus.DONE, TieredTransitTimeVarianceStatus.FAILED -> return true
                }
                return false
            }
            TieredTransitTimeVarianceStatus.RUNNING -> {
                when (next) {
                    TieredTransitTimeVarianceStatus.FAILED -> return true
                }
                return false
            }
            TieredTransitTimeVarianceStatus.DONE -> {
                when (next) {
                    TieredTransitTimeVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTransitTimeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("tiered transit time variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((budget - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): TieredTransitTimeVariance {
        return TieredTransitTimeVariance(
            code = code,
            name = name,
            stopCount = stopCount,
            lane = lane,
            minimum = minimum,
            shiftCode = shiftCode,
            budget = budget,
            durationMinutes = durationMinutes,
            source = source,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered transit time variance in status new. */
fun newTieredTransitTimeVariance(code: String, name: String): TieredTransitTimeVariance {
    return TieredTransitTimeVariance(
        code = code,
        name = name,
        stopCount = 0,
        lane = "",
        minimum = 0.0,
        shiftCode = "",
        budget = 0.0,
        durationMinutes = 0,
        source = "",
        priority = 0,
        status = TieredTransitTimeVarianceStatus.NEW,
    )
}
