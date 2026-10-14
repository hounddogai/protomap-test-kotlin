package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection checkpoint is in its life cycle. */
enum class PendingInspectionCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending inspection checkpoint, with what the planners need to act on it. */
data class PendingInspectionCheckpoint(
    var code: String,
    var name: String,
    var externalRef: String,
    var currency: String,
    var routeCode: String,
    var limit: Int,
    var distanceKm: Double,
    var segment: String,
    var amount: Double,
    var lane: String,
    var status: PendingInspectionCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionCheckpointStatus): Boolean {
        when (status) {
            PendingInspectionCheckpointStatus.CREATED -> {
                when (next) {
                    PendingInspectionCheckpointStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingInspectionCheckpointStatus.VALIDATED -> {
                when (next) {
                    PendingInspectionCheckpointStatus.PUBLISHED, PendingInspectionCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            PendingInspectionCheckpointStatus.PUBLISHED -> return next == PendingInspectionCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingInspectionCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((distanceKm - limit.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionCheckpoint {
        return PendingInspectionCheckpoint(
            code = code,
            name = name,
            externalRef = externalRef,
            currency = currency,
            routeCode = routeCode,
            limit = limit,
            distanceKm = distanceKm,
            segment = segment,
            amount = amount,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a pending inspection checkpoint in status created. */
fun newPendingInspectionCheckpoint(code: String, name: String): PendingInspectionCheckpoint {
    return PendingInspectionCheckpoint(
        code = code,
        name = name,
        externalRef = "",
        currency = "",
        routeCode = "",
        limit = 0,
        distanceKm = 0.0,
        segment = "",
        amount = 0.0,
        lane = "",
        status = PendingInspectionCheckpointStatus.CREATED,
    )
}
