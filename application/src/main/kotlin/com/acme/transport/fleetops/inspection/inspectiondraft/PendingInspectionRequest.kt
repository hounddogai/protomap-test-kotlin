package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection request is in its life cycle. */
enum class PendingInspectionRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One pending inspection request, with what the planners need to act on it. */
data class PendingInspectionRequest(
    var code: String,
    var name: String,
    var lane: String,
    var graceDays: Int,
    var floorValue: Double,
    var stopCount: Int,
    var externalRef: String,
    var ratio: Double,
    var pallets: Int,
    var minimum: Double,
    var checksum: String,
    var status: PendingInspectionRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionRequestStatus): Boolean {
        when (status) {
            PendingInspectionRequestStatus.PROPOSED -> {
                when (next) {
                    PendingInspectionRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
            PendingInspectionRequestStatus.APPROVED -> return next == PendingInspectionRequestStatus.WITHDRAWN
            PendingInspectionRequestStatus.REJECTED -> {
                when (next) {
                    PendingInspectionRequestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingInspectionRequestStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedTotal(): Double = round((ratio + pallets.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionRequest {
        return PendingInspectionRequest(
            code = code,
            name = name,
            lane = lane,
            graceDays = graceDays,
            floorValue = floorValue,
            stopCount = stopCount,
            externalRef = externalRef,
            ratio = ratio,
            pallets = pallets,
            minimum = minimum,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a pending inspection request in status proposed. */
fun newPendingInspectionRequest(code: String, name: String): PendingInspectionRequest {
    return PendingInspectionRequest(
        code = code,
        name = name,
        lane = "",
        graceDays = 0,
        floorValue = 0.0,
        stopCount = 0,
        externalRef = "",
        ratio = 0.0,
        pallets = 0,
        minimum = 0.0,
        checksum = "",
        status = PendingInspectionRequestStatus.PROPOSED,
    )
}
