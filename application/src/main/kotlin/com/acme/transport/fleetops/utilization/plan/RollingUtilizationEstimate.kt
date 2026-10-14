package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.kit.text.normalize

/** Where a rolling utilization estimate is in its life cycle. */
enum class RollingUtilizationEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling utilization estimate as the utilization team keeps it. */
data class RollingUtilizationEstimate(
    var code: String,
    var name: String,
    var revision: Int,
    var vehicleId: String,
    var checksum: String,
    var status: RollingUtilizationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingUtilizationEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingUtilizationEstimateStatus): Boolean {
        when (status) {
            RollingUtilizationEstimateStatus.IDLE -> {
                when (next) {
                    RollingUtilizationEstimateStatus.DRAINING, RollingUtilizationEstimateStatus.STOPPED -> return true
                }
                return false
            }
            RollingUtilizationEstimateStatus.WARMING -> {
                when (next) {
                    RollingUtilizationEstimateStatus.DRAINING -> return true
                }
                return false
            }
            RollingUtilizationEstimateStatus.READY -> return next == RollingUtilizationEstimateStatus.STOPPED
            RollingUtilizationEstimateStatus.DRAINING -> return next == RollingUtilizationEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingUtilizationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("rolling utilization estimate $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): RollingUtilizationEstimate {
        return RollingUtilizationEstimate(
            code = code,
            name = name,
            revision = revision,
            vehicleId = vehicleId,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling utilization estimate in status idle. */
fun newRollingUtilizationEstimate(code: String, name: String): RollingUtilizationEstimate {
    return RollingUtilizationEstimate(
        code = code,
        name = name,
        revision = 0,
        vehicleId = "",
        checksum = "",
        status = RollingUtilizationEstimateStatus.IDLE,
    )
}
