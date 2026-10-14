package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan request is in its life cycle. */
enum class LanePlanRequestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lane plan request. Its code is unique within a tenant. */
data class LanePlanRequest(
    var code: String,
    var label: String,
    var pallets: Int,
    var rate: Double,
    var limit: Int,
    var status: LanePlanRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanRequestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanRequestStatus): Boolean {
        when (status) {
            LanePlanRequestStatus.PLANNED -> {
                when (next) {
                    LanePlanRequestStatus.IN_PROGRESS, LanePlanRequestStatus.COMPLETED -> return true
                }
                return false
            }
            LanePlanRequestStatus.SCHEDULED -> {
                when (next) {
                    LanePlanRequestStatus.CANCELLED -> return true
                }
                return false
            }
            LanePlanRequestStatus.IN_PROGRESS -> return next == LanePlanRequestStatus.CANCELLED
            LanePlanRequestStatus.COMPLETED -> return next == LanePlanRequestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LanePlanRequestStatus) {
        if (!canMoveTo(next)) {
            error("lane plan request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"lane plan request"} $code"

    fun effectiveTotal(): Double = round((rate + pallets.toDouble()) * 100) / 100

    fun snapshot(): LanePlanRequest {
        return LanePlanRequest(
            code = code,
            label = label,
            pallets = pallets,
            rate = rate,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lane plan request in status planned. */
fun newLanePlanRequest(code: String, label: String): LanePlanRequest {
    return LanePlanRequest(
        code = code,
        label = label,
        pallets = 0,
        rate = 0.0,
        limit = 0,
        status = LanePlanRequestStatus.PLANNED,
    )
}
