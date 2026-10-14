package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending idling variance is in its life cycle. */
enum class PendingIdlingVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending idling variance and its current status. */
data class PendingIdlingVariance(
    var code: String,
    var name: String,
    var weight: Double,
    var region: String,
    var notes: String,
    var depot: String,
    var active: Boolean,
    var limit: Int,
    var trailerId: String,
    var checksum: String,
    var status: PendingIdlingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingIdlingVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingIdlingVarianceStatus): Boolean {
        when (status) {
            PendingIdlingVarianceStatus.CREATED -> return next == PendingIdlingVarianceStatus.VALIDATED || next == PendingIdlingVarianceStatus.PUBLISHED
            PendingIdlingVarianceStatus.VALIDATED -> return next == PendingIdlingVarianceStatus.PUBLISHED || next == PendingIdlingVarianceStatus.RETIRED
            PendingIdlingVarianceStatus.PUBLISHED -> return next == PendingIdlingVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingIdlingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("pending idling variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedExposure(): Double = round((limit.toDouble() - weight) * 100) / 100

    fun snapshot(): PendingIdlingVariance {
        return PendingIdlingVariance(
            code = code,
            name = name,
            weight = weight,
            region = region,
            notes = notes,
            depot = depot,
            active = active,
            limit = limit,
            trailerId = trailerId,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending idling variance in status created. */
fun newPendingIdlingVariance(code: String, name: String): PendingIdlingVariance {
    return PendingIdlingVariance(
        code = code,
        name = name,
        weight = 0.0,
        region = "",
        notes = "",
        depot = "",
        active = false,
        limit = 0,
        trailerId = "",
        checksum = "",
        status = PendingIdlingVarianceStatus.CREATED,
    )
}
