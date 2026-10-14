package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery request is in its life cycle. */
enum class ProofOfDeliveryRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a proof of delivery request and its current status. */
data class ProofOfDeliveryRequest(
    var code: String,
    var label: String,
    var routeCode: String,
    var checksum: String,
    var active: Boolean,
    var archived: Boolean,
    var cost: Double,
    var durationMinutes: Int,
    var graceDays: Int,
    var notes: String,
    var discount: Double,
    var status: ProofOfDeliveryRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliveryRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliveryRequestStatus): Boolean {
        when (status) {
            ProofOfDeliveryRequestStatus.DRAFT -> return next == ProofOfDeliveryRequestStatus.PENDING || next == ProofOfDeliveryRequestStatus.CLOSED
            ProofOfDeliveryRequestStatus.PENDING -> return next == ProofOfDeliveryRequestStatus.ACTIVE
            ProofOfDeliveryRequestStatus.ACTIVE -> return next == ProofOfDeliveryRequestStatus.SUSPENDED
            ProofOfDeliveryRequestStatus.SUSPENDED -> return next == ProofOfDeliveryRequestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ProofOfDeliveryRequestStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"proof of delivery request"} $code"

    fun effectiveLoad(): Double = round((graceDays.toDouble() - discount) * 100) / 100

    fun snapshot(): ProofOfDeliveryRequest {
        return ProofOfDeliveryRequest(
            code = code,
            label = label,
            routeCode = routeCode,
            checksum = checksum,
            active = active,
            archived = archived,
            cost = cost,
            durationMinutes = durationMinutes,
            graceDays = graceDays,
            notes = notes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a proof of delivery request in status draft. */
fun newProofOfDeliveryRequest(code: String, label: String): ProofOfDeliveryRequest {
    return ProofOfDeliveryRequest(
        code = code,
        label = label,
        routeCode = "",
        checksum = "",
        active = false,
        archived = false,
        cost = 0.0,
        durationMinutes = 0,
        graceDays = 0,
        notes = "",
        discount = 0.0,
        status = ProofOfDeliveryRequestStatus.DRAFT,
    )
}
