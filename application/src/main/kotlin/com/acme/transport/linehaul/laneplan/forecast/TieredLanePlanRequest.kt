package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered lane plan request is in its life cycle. */
enum class TieredLanePlanRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered lane plan request. Its code is unique within a tenant. */
data class TieredLanePlanRequest(
    var code: String,
    var label: String,
    var maximum: Double,
    var depot: String,
    var batchSize: Int,
    var status: TieredLanePlanRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredLanePlanRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredLanePlanRequestStatus): Boolean {
        when (status) {
            TieredLanePlanRequestStatus.IDLE -> return next == TieredLanePlanRequestStatus.WARMING || next == TieredLanePlanRequestStatus.DRAINING
            TieredLanePlanRequestStatus.WARMING -> {
                when (next) {
                    TieredLanePlanRequestStatus.READY -> return true
                }
                return false
            }
            TieredLanePlanRequestStatus.READY -> {
                when (next) {
                    TieredLanePlanRequestStatus.DRAINING, TieredLanePlanRequestStatus.STOPPED -> return true
                }
                return false
            }
            TieredLanePlanRequestStatus.DRAINING -> {
                when (next) {
                    TieredLanePlanRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredLanePlanRequestStatus) {
        if (!canMoveTo(next)) {
            error("tiered lane plan request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(maximum * batchSize.toDouble() * 100) / 100

    fun snapshot(): TieredLanePlanRequest {
        return TieredLanePlanRequest(
            code = code,
            label = label,
            maximum = maximum,
            depot = depot,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered lane plan request in status idle. */
fun newTieredLanePlanRequest(code: String, label: String): TieredLanePlanRequest {
    return TieredLanePlanRequest(
        code = code,
        label = label,
        maximum = 0.0,
        depot = "",
        batchSize = 0,
        status = TieredLanePlanRequestStatus.IDLE,
    )
}
