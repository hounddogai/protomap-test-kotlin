package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending redelivery request is in its life cycle. */
enum class PendingRedeliveryRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending redelivery request. Its code is unique within a tenant. */
data class PendingRedeliveryRequest(
    var code: String,
    var title: String,
    var graceDays: Int,
    var driverId: String,
    var distanceKm: Double,
    var budget: Double,
    var status: PendingRedeliveryRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRedeliveryRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRedeliveryRequestStatus): Boolean {
        when (status) {
            PendingRedeliveryRequestStatus.CREATED -> {
                when (next) {
                    PendingRedeliveryRequestStatus.VALIDATED, PendingRedeliveryRequestStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingRedeliveryRequestStatus.VALIDATED -> {
                when (next) {
                    PendingRedeliveryRequestStatus.RETIRED -> return true
                }
                return false
            }
            PendingRedeliveryRequestStatus.PUBLISHED -> {
                when (next) {
                    PendingRedeliveryRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRedeliveryRequestStatus) {
        if (!canMoveTo(next)) {
            error("pending redelivery request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending redelivery request"} $code"

    fun projectedValue(): Double = round(distanceKm * graceDays.toDouble() * 100) / 100

    fun snapshot(): PendingRedeliveryRequest {
        return PendingRedeliveryRequest(
            code = code,
            title = title,
            graceDays = graceDays,
            driverId = driverId,
            distanceKm = distanceKm,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending redelivery request in status created. */
fun newPendingRedeliveryRequest(code: String, title: String): PendingRedeliveryRequest {
    return PendingRedeliveryRequest(
        code = code,
        title = title,
        graceDays = 0,
        driverId = "",
        distanceKm = 0.0,
        budget = 0.0,
        status = PendingRedeliveryRequestStatus.CREATED,
    )
}
