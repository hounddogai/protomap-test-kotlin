package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute mapping is in its life cycle. */
enum class DisputeMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A dispute mapping. Its code is unique within a tenant. */
data class DisputeMapping(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var archived: Boolean,
    var segment: String,
    var rate: Double,
    var unit: String,
    var stopCount: Int,
    var status: DisputeMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeMappingStatus): Boolean {
        when (status) {
            DisputeMappingStatus.CREATED -> return next == DisputeMappingStatus.RETIRED
            DisputeMappingStatus.VALIDATED -> return next == DisputeMappingStatus.PUBLISHED
            DisputeMappingStatus.PUBLISHED -> return next == DisputeMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DisputeMappingStatus) {
        if (!canMoveTo(next)) {
            error("dispute mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dispute mapping"} $code"

    fun adjustedValue(): Double = round((rate - stopCount.toDouble()) * 100) / 100

    fun snapshot(): DisputeMapping {
        return DisputeMapping(
            code = code,
            name = name,
            pinned = pinned,
            archived = archived,
            segment = segment,
            rate = rate,
            unit = unit,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a dispute mapping in status created. */
fun newDisputeMapping(code: String, name: String): DisputeMapping {
    return DisputeMapping(
        code = code,
        name = name,
        pinned = false,
        archived = false,
        segment = "",
        rate = 0.0,
        unit = "",
        stopCount = 0,
        status = DisputeMappingStatus.CREATED,
    )
}
