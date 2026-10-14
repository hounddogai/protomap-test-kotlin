package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell estimate is in its life cycle. */
enum class DwellEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a dwell estimate and its current status. */
data class DwellEstimate(
    var code: String,
    var title: String,
    var floorValue: Double,
    var region: String,
    var startsAt: Int,
    var budget: Double,
    var contactEmail: String,
    var windowDays: Int,
    var category: String,
    var status: DwellEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellEstimateStatus): Boolean {
        when (status) {
            DwellEstimateStatus.IDLE -> return next == DwellEstimateStatus.DRAINING
            DwellEstimateStatus.WARMING -> return next == DwellEstimateStatus.STOPPED
            DwellEstimateStatus.READY -> return next == DwellEstimateStatus.DRAINING || next == DwellEstimateStatus.STOPPED
            DwellEstimateStatus.DRAINING -> {
                when (next) {
                    DwellEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellEstimateStatus) {
        if (!canMoveTo(next)) {
            error("dwell estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dwell estimate"} $code"

    fun weightedTotal(): Double = round(windowDays.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): DwellEstimate {
        return DwellEstimate(
            code = code,
            title = title,
            floorValue = floorValue,
            region = region,
            startsAt = startsAt,
            budget = budget,
            contactEmail = contactEmail,
            windowDays = windowDays,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a dwell estimate in status idle. */
fun newDwellEstimate(code: String, title: String): DwellEstimate {
    return DwellEstimate(
        code = code,
        title = title,
        floorValue = 0.0,
        region = "",
        startsAt = 0,
        budget = 0.0,
        contactEmail = "",
        windowDays = 0,
        category = "",
        status = DwellEstimateStatus.IDLE,
    )
}
