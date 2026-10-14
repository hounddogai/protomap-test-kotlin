package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped proof of delivery group is in its life cycle. */
enum class CappedProofOfDeliveryGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a capped proof of delivery group and its current status. */
data class CappedProofOfDeliveryGroup(
    var code: String,
    var label: String,
    var unit: String,
    var sequence: Int,
    var createdAt: Int,
    var trailerId: String,
    var updatedAt: Int,
    var windowDays: Int,
    var status: CappedProofOfDeliveryGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedProofOfDeliveryGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedProofOfDeliveryGroupStatus): Boolean {
        when (status) {
            CappedProofOfDeliveryGroupStatus.REQUESTED -> return next == CappedProofOfDeliveryGroupStatus.REVOKED || next == CappedProofOfDeliveryGroupStatus.EXPIRED
            CappedProofOfDeliveryGroupStatus.GRANTED -> return next == CappedProofOfDeliveryGroupStatus.REVOKED || next == CappedProofOfDeliveryGroupStatus.EXPIRED
            CappedProofOfDeliveryGroupStatus.REVOKED -> return next == CappedProofOfDeliveryGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CappedProofOfDeliveryGroupStatus) {
        if (!canMoveTo(next)) {
            error("capped proof of delivery group $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped proof of delivery group"} $code"

    fun projectedTotal(): Double = round((createdAt.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): CappedProofOfDeliveryGroup {
        return CappedProofOfDeliveryGroup(
            code = code,
            label = label,
            unit = unit,
            sequence = sequence,
            createdAt = createdAt,
            trailerId = trailerId,
            updatedAt = updatedAt,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped proof of delivery group in status requested. */
fun newCappedProofOfDeliveryGroup(code: String, label: String): CappedProofOfDeliveryGroup {
    return CappedProofOfDeliveryGroup(
        code = code,
        label = label,
        unit = "",
        sequence = 0,
        createdAt = 0,
        trailerId = "",
        updatedAt = 0,
        windowDays = 0,
        status = CappedProofOfDeliveryGroupStatus.REQUESTED,
    )
}
