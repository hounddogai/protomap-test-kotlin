package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a proof of delivery snapshot is in its life cycle. */
enum class ProofOfDeliverySnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A proof of delivery snapshot. Its code is unique within a tenant. */
data class ProofOfDeliverySnapshot(
    var code: String,
    var name: String,
    var active: Boolean,
    var weight: Double,
    var attempts: Int,
    var revision: Int,
    var timezone: String,
    var routeCode: String,
    var expiresAt: Int,
    var vehicleId: String,
    var windowDays: Int,
    var status: ProofOfDeliverySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ProofOfDeliverySnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ProofOfDeliverySnapshotStatus): Boolean {
        when (status) {
            ProofOfDeliverySnapshotStatus.PROPOSED -> {
                when (next) {
                    ProofOfDeliverySnapshotStatus.REJECTED, ProofOfDeliverySnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliverySnapshotStatus.APPROVED -> {
                when (next) {
                    ProofOfDeliverySnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            ProofOfDeliverySnapshotStatus.REJECTED -> return next == ProofOfDeliverySnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ProofOfDeliverySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("proof of delivery snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((windowDays.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ProofOfDeliverySnapshot {
        return ProofOfDeliverySnapshot(
            code = code,
            name = name,
            active = active,
            weight = weight,
            attempts = attempts,
            revision = revision,
            timezone = timezone,
            routeCode = routeCode,
            expiresAt = expiresAt,
            vehicleId = vehicleId,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a proof of delivery snapshot in status proposed. */
fun newProofOfDeliverySnapshot(code: String, name: String): ProofOfDeliverySnapshot {
    return ProofOfDeliverySnapshot(
        code = code,
        name = name,
        active = false,
        weight = 0.0,
        attempts = 0,
        revision = 0,
        timezone = "",
        routeCode = "",
        expiresAt = 0,
        vehicleId = "",
        windowDays = 0,
        status = ProofOfDeliverySnapshotStatus.PROPOSED,
    )
}
