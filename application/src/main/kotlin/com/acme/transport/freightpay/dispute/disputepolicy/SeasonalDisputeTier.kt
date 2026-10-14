package com.acme.transport.freightpay.dispute.disputepolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal dispute tier is in its life cycle. */
enum class SeasonalDisputeTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a seasonal dispute tier and its current status. */
data class SeasonalDisputeTier(
    var code: String,
    var title: String,
    var shiftCode: String,
    var windowDays: Int,
    var maximum: Double,
    var trailerId: String,
    var region: String,
    var budget: Double,
    var status: SeasonalDisputeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDisputeTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDisputeTierStatus): Boolean {
        when (status) {
            SeasonalDisputeTierStatus.NEW -> {
                when (next) {
                    SeasonalDisputeTierStatus.RUNNING, SeasonalDisputeTierStatus.DONE -> return true
                }
                return false
            }
            SeasonalDisputeTierStatus.QUEUED -> return next == SeasonalDisputeTierStatus.FAILED
            SeasonalDisputeTierStatus.RUNNING -> {
                when (next) {
                    SeasonalDisputeTierStatus.DONE -> return true
                }
                return false
            }
            SeasonalDisputeTierStatus.DONE -> return next == SeasonalDisputeTierStatus.FAILED
        }
        return false
    }

    fun moveTo(next: SeasonalDisputeTierStatus) {
        if (!canMoveTo(next)) {
            error("seasonal dispute tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal dispute tier"} $code"

    fun netTotal(): Double = round(windowDays.toDouble() * maximum * 100) / 100

    fun snapshot(): SeasonalDisputeTier {
        return SeasonalDisputeTier(
            code = code,
            title = title,
            shiftCode = shiftCode,
            windowDays = windowDays,
            maximum = maximum,
            trailerId = trailerId,
            region = region,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal dispute tier in status new. */
fun newSeasonalDisputeTier(code: String, title: String): SeasonalDisputeTier {
    return SeasonalDisputeTier(
        code = code,
        title = title,
        shiftCode = "",
        windowDays = 0,
        maximum = 0.0,
        trailerId = "",
        region = "",
        budget = 0.0,
        status = SeasonalDisputeTierStatus.NEW,
    )
}
