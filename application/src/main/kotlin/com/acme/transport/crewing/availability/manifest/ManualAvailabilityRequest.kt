package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual availability request is in its life cycle. */
enum class ManualAvailabilityRequestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual availability request as the availability team keeps it. */
data class ManualAvailabilityRequest(
    var code: String,
    var title: String,
    var margin: Double,
    var rate: Double,
    var limit: Int,
    var status: ManualAvailabilityRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAvailabilityRequestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAvailabilityRequestStatus): Boolean {
        when (status) {
            ManualAvailabilityRequestStatus.CREATED -> return next == ManualAvailabilityRequestStatus.VALIDATED || next == ManualAvailabilityRequestStatus.RETIRED
            ManualAvailabilityRequestStatus.VALIDATED -> {
                when (next) {
                    ManualAvailabilityRequestStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualAvailabilityRequestStatus.PUBLISHED -> {
                when (next) {
                    ManualAvailabilityRequestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAvailabilityRequestStatus) {
        if (!canMoveTo(next)) {
            error("manual availability request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round((limit.toDouble() + margin) * 100) / 100

    fun snapshot(): ManualAvailabilityRequest {
        return ManualAvailabilityRequest(
            code = code,
            title = title,
            margin = margin,
            rate = rate,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual availability request in status created. */
fun newManualAvailabilityRequest(code: String, title: String): ManualAvailabilityRequest {
    return ManualAvailabilityRequest(
        code = code,
        title = title,
        margin = 0.0,
        rate = 0.0,
        limit = 0,
        status = ManualAvailabilityRequestStatus.CREATED,
    )
}
