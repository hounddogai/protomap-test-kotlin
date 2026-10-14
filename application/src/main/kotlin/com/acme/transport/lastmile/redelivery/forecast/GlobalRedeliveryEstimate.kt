package com.acme.transport.lastmile.redelivery.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global redelivery estimate is in its life cycle. */
enum class GlobalRedeliveryEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global redelivery estimate as the redelivery team keeps it. */
data class GlobalRedeliveryEstimate(
    var code: String,
    var title: String,
    var amount: Double,
    var driverId: String,
    var ratio: Double,
    var source: String,
    var status: GlobalRedeliveryEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRedeliveryEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRedeliveryEstimateStatus): Boolean {
        when (status) {
            GlobalRedeliveryEstimateStatus.IDLE -> return next == GlobalRedeliveryEstimateStatus.DRAINING || next == GlobalRedeliveryEstimateStatus.STOPPED
            GlobalRedeliveryEstimateStatus.WARMING -> return next == GlobalRedeliveryEstimateStatus.STOPPED
            GlobalRedeliveryEstimateStatus.READY -> {
                when (next) {
                    GlobalRedeliveryEstimateStatus.DRAINING -> return true
                }
                return false
            }
            GlobalRedeliveryEstimateStatus.DRAINING -> {
                when (next) {
                    GlobalRedeliveryEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRedeliveryEstimateStatus) {
        if (!canMoveTo(next)) {
            error("global redelivery estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round(amount * ratio * 100) / 100

    fun snapshot(): GlobalRedeliveryEstimate {
        return GlobalRedeliveryEstimate(
            code = code,
            title = title,
            amount = amount,
            driverId = driverId,
            ratio = ratio,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global redelivery estimate in status idle. */
fun newGlobalRedeliveryEstimate(code: String, title: String): GlobalRedeliveryEstimate {
    return GlobalRedeliveryEstimate(
        code = code,
        title = title,
        amount = 0.0,
        driverId = "",
        ratio = 0.0,
        source = "",
        status = GlobalRedeliveryEstimateStatus.IDLE,
    )
}
