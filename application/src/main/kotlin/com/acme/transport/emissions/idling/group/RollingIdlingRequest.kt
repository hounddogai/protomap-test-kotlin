package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling idling request is in its life cycle. */
enum class RollingIdlingRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling idling request as the idling team keeps it. */
data class RollingIdlingRequest(
    var code: String,
    var title: String,
    var phone: String,
    var threshold: Double,
    var routeCode: String,
    var discount: Double,
    var maximum: Double,
    var status: RollingIdlingRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIdlingRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIdlingRequestStatus): Boolean {
        when (status) {
            RollingIdlingRequestStatus.CREATED -> {
                when (next) {
                    RollingIdlingRequestStatus.VALIDATED, RollingIdlingRequestStatus.PUBLISHED -> return true
                }
                return false
            }
            RollingIdlingRequestStatus.VALIDATED -> return next == RollingIdlingRequestStatus.PUBLISHED || next == RollingIdlingRequestStatus.RETIRED
            RollingIdlingRequestStatus.PUBLISHED -> {
                when (next) {
                    RollingIdlingRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingIdlingRequestStatus) {
        if (!canMoveTo(next)) {
            error("rolling idling request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((threshold - maximum) * 100) / 100

    fun snapshot(): RollingIdlingRequest {
        return RollingIdlingRequest(
            code = code,
            title = title,
            phone = phone,
            threshold = threshold,
            routeCode = routeCode,
            discount = discount,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling idling request in status created. */
fun newRollingIdlingRequest(code: String, title: String): RollingIdlingRequest {
    return RollingIdlingRequest(
        code = code,
        title = title,
        phone = "",
        threshold = 0.0,
        routeCode = "",
        discount = 0.0,
        maximum = 0.0,
        status = RollingIdlingRequestStatus.CREATED,
    )
}
