package com.acme.transport.fleetops.defect.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect request is in its life cycle. */
enum class DefectRequestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a defect request and its current status. */
data class DefectRequest(
    var code: String,
    var name: String,
    var channel: String,
    var ownerId: String,
    var fuelLiters: Double,
    var currency: String,
    var externalRef: String,
    var archived: Boolean,
    var sequence: Int,
    var active: Boolean,
    var status: DefectRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectRequestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectRequestStatus): Boolean {
        when (status) {
            DefectRequestStatus.PROPOSED -> return next == DefectRequestStatus.REJECTED || next == DefectRequestStatus.WITHDRAWN
            DefectRequestStatus.APPROVED -> return next == DefectRequestStatus.WITHDRAWN
            DefectRequestStatus.REJECTED -> return next == DefectRequestStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DefectRequestStatus) {
        if (!canMoveTo(next)) {
            error("defect request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((fuelLiters - sequence.toDouble()) * 100) / 100

    fun snapshot(): DefectRequest {
        return DefectRequest(
            code = code,
            name = name,
            channel = channel,
            ownerId = ownerId,
            fuelLiters = fuelLiters,
            currency = currency,
            externalRef = externalRef,
            archived = archived,
            sequence = sequence,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a defect request in status proposed. */
fun newDefectRequest(code: String, name: String): DefectRequest {
    return DefectRequest(
        code = code,
        name = name,
        channel = "",
        ownerId = "",
        fuelLiters = 0.0,
        currency = "",
        externalRef = "",
        archived = false,
        sequence = 0,
        active = false,
        status = DefectRequestStatus.PROPOSED,
    )
}
