package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback service plan request is in its life cycle. */
enum class FallbackServicePlanRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback service plan request. Its code is unique within a tenant. */
data class FallbackServicePlanRequest(
    var code: String,
    var name: String,
    var contactEmail: String,
    var priority: Int,
    var driverId: String,
    var archived: Boolean,
    var retries: Int,
    var visible: Boolean,
    var status: FallbackServicePlanRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServicePlanRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServicePlanRequestStatus): Boolean {
        when (status) {
            FallbackServicePlanRequestStatus.CREATED -> {
                when (next) {
                    FallbackServicePlanRequestStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackServicePlanRequestStatus.VALIDATED -> {
                when (next) {
                    FallbackServicePlanRequestStatus.PUBLISHED, FallbackServicePlanRequestStatus.RETIRED -> return true
                }
                return false
            }
            FallbackServicePlanRequestStatus.PUBLISHED -> {
                when (next) {
                    FallbackServicePlanRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackServicePlanRequestStatus) {
        if (!canMoveTo(next)) {
            error("fallback service plan request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round(priority.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): FallbackServicePlanRequest {
        return FallbackServicePlanRequest(
            code = code,
            name = name,
            contactEmail = contactEmail,
            priority = priority,
            driverId = driverId,
            archived = archived,
            retries = retries,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback service plan request in status created. */
fun newFallbackServicePlanRequest(code: String, name: String): FallbackServicePlanRequest {
    return FallbackServicePlanRequest(
        code = code,
        name = name,
        contactEmail = "",
        priority = 0,
        driverId = "",
        archived = false,
        retries = 0,
        visible = false,
        status = FallbackServicePlanRequestStatus.CREATED,
    )
}
