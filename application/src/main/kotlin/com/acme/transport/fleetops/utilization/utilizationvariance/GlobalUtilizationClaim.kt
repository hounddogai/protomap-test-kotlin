package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global utilization claim is in its life cycle. */
enum class GlobalUtilizationClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a global utilization claim and its current status. */
data class GlobalUtilizationClaim(
    var code: String,
    var title: String,
    var active: Boolean,
    var checksum: String,
    var notes: String,
    var capacity: Int,
    var odometer: Int,
    var batchSize: Int,
    var fuelLiters: Double,
    var status: GlobalUtilizationClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalUtilizationClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalUtilizationClaimStatus): Boolean {
        when (status) {
            GlobalUtilizationClaimStatus.NEW -> {
                when (next) {
                    GlobalUtilizationClaimStatus.QUEUED, GlobalUtilizationClaimStatus.RUNNING -> return true
                }
                return false
            }
            GlobalUtilizationClaimStatus.QUEUED -> return next == GlobalUtilizationClaimStatus.RUNNING || next == GlobalUtilizationClaimStatus.FAILED
            GlobalUtilizationClaimStatus.RUNNING -> {
                when (next) {
                    GlobalUtilizationClaimStatus.DONE, GlobalUtilizationClaimStatus.FAILED -> return true
                }
                return false
            }
            GlobalUtilizationClaimStatus.DONE -> return next == GlobalUtilizationClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalUtilizationClaimStatus) {
        if (!canMoveTo(next)) {
            error("global utilization claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global utilization claim"} $code"

    fun netTotal(): Double = round((batchSize.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): GlobalUtilizationClaim {
        return GlobalUtilizationClaim(
            code = code,
            title = title,
            active = active,
            checksum = checksum,
            notes = notes,
            capacity = capacity,
            odometer = odometer,
            batchSize = batchSize,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global utilization claim in status new. */
fun newGlobalUtilizationClaim(code: String, title: String): GlobalUtilizationClaim {
    return GlobalUtilizationClaim(
        code = code,
        title = title,
        active = false,
        checksum = "",
        notes = "",
        capacity = 0,
        odometer = 0,
        batchSize = 0,
        fuelLiters = 0.0,
        status = GlobalUtilizationClaimStatus.NEW,
    )
}
