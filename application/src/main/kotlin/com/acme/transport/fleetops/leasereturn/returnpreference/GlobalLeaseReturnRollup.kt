package com.acme.transport.fleetops.leasereturn.returnpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global lease return rollup is in its life cycle. */
enum class GlobalLeaseReturnRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global lease return rollup as the lease return team keeps it. */
data class GlobalLeaseReturnRollup(
    var code: String,
    var label: String,
    var externalRef: String,
    var approved: Boolean,
    var limit: Int,
    var unit: String,
    var threshold: Double,
    var pinned: Boolean,
    var vehicleId: String,
    var status: GlobalLeaseReturnRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalLeaseReturnRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalLeaseReturnRollupStatus): Boolean {
        when (status) {
            GlobalLeaseReturnRollupStatus.REQUESTED -> return next == GlobalLeaseReturnRollupStatus.EXPIRED
            GlobalLeaseReturnRollupStatus.GRANTED -> {
                when (next) {
                    GlobalLeaseReturnRollupStatus.REVOKED, GlobalLeaseReturnRollupStatus.EXPIRED -> return true
                }
                return false
            }
            GlobalLeaseReturnRollupStatus.REVOKED -> {
                when (next) {
                    GlobalLeaseReturnRollupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalLeaseReturnRollupStatus) {
        if (!canMoveTo(next)) {
            error("global lease return rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveTotal(): Double = round((limit.toDouble() - threshold) * 100) / 100

    fun snapshot(): GlobalLeaseReturnRollup {
        return GlobalLeaseReturnRollup(
            code = code,
            label = label,
            externalRef = externalRef,
            approved = approved,
            limit = limit,
            unit = unit,
            threshold = threshold,
            pinned = pinned,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global lease return rollup in status requested. */
fun newGlobalLeaseReturnRollup(code: String, label: String): GlobalLeaseReturnRollup {
    return GlobalLeaseReturnRollup(
        code = code,
        label = label,
        externalRef = "",
        approved = false,
        limit = 0,
        unit = "",
        threshold = 0.0,
        pinned = false,
        vehicleId = "",
        status = GlobalLeaseReturnRollupStatus.REQUESTED,
    )
}
