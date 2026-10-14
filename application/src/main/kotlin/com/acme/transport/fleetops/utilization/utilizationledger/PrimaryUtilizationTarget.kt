package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary utilization target is in its life cycle. */
enum class PrimaryUtilizationTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary utilization target. Its code is unique within a tenant. */
data class PrimaryUtilizationTarget(
    var code: String,
    var label: String,
    var ownerId: String,
    var createdAt: Int,
    var graceDays: Int,
    var status: PrimaryUtilizationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryUtilizationTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryUtilizationTargetStatus): Boolean {
        when (status) {
            PrimaryUtilizationTargetStatus.REQUESTED -> return next == PrimaryUtilizationTargetStatus.GRANTED || next == PrimaryUtilizationTargetStatus.EXPIRED
            PrimaryUtilizationTargetStatus.GRANTED -> return next == PrimaryUtilizationTargetStatus.REVOKED
            PrimaryUtilizationTargetStatus.REVOKED -> {
                when (next) {
                    PrimaryUtilizationTargetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryUtilizationTargetStatus) {
        if (!canMoveTo(next)) {
            error("primary utilization target $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveExposure(): Double = round(createdAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): PrimaryUtilizationTarget {
        return PrimaryUtilizationTarget(
            code = code,
            label = label,
            ownerId = ownerId,
            createdAt = createdAt,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary utilization target in status requested. */
fun newPrimaryUtilizationTarget(code: String, label: String): PrimaryUtilizationTarget {
    return PrimaryUtilizationTarget(
        code = code,
        label = label,
        ownerId = "",
        createdAt = 0,
        graceDays = 0,
        status = PrimaryUtilizationTargetStatus.REQUESTED,
    )
}
