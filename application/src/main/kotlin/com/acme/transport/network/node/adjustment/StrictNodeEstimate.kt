package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict node estimate is in its life cycle. */
enum class StrictNodeEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict node estimate as the node team keeps it. */
data class StrictNodeEstimate(
    var code: String,
    var title: String,
    var amount: Double,
    var etaMinutes: Int,
    var routeCode: String,
    var ownerId: String,
    var updatedAt: Int,
    var carrier: String,
    var status: StrictNodeEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictNodeEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictNodeEstimateStatus): Boolean {
        when (status) {
            StrictNodeEstimateStatus.NEW -> return next == StrictNodeEstimateStatus.RUNNING
            StrictNodeEstimateStatus.QUEUED -> {
                when (next) {
                    StrictNodeEstimateStatus.DONE -> return true
                }
                return false
            }
            StrictNodeEstimateStatus.RUNNING -> {
                when (next) {
                    StrictNodeEstimateStatus.DONE -> return true
                }
                return false
            }
            StrictNodeEstimateStatus.DONE -> return next == StrictNodeEstimateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictNodeEstimateStatus) {
        if (!canMoveTo(next)) {
            error("strict node estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round((etaMinutes.toDouble() - amount) * 100) / 100

    fun snapshot(): StrictNodeEstimate {
        return StrictNodeEstimate(
            code = code,
            title = title,
            amount = amount,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            ownerId = ownerId,
            updatedAt = updatedAt,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict node estimate in status new. */
fun newStrictNodeEstimate(code: String, title: String): StrictNodeEstimate {
    return StrictNodeEstimate(
        code = code,
        title = title,
        amount = 0.0,
        etaMinutes = 0,
        routeCode = "",
        ownerId = "",
        updatedAt = 0,
        carrier = "",
        status = StrictNodeEstimateStatus.NEW,
    )
}
