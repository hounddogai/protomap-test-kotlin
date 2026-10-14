package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization estimate is in its life cycle. */
enum class UtilizationEstimateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One utilization estimate, with what the planners need to act on it. */
data class UtilizationEstimate(
    var code: String,
    var name: String,
    var title: String,
    var depot: String,
    var checksum: String,
    var tags: MutableList<String>,
    var attempts: Int,
    var loadKg: Double,
    var status: UtilizationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationEstimateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationEstimateStatus): Boolean {
        when (status) {
            UtilizationEstimateStatus.REQUESTED -> return next == UtilizationEstimateStatus.REVOKED || next == UtilizationEstimateStatus.EXPIRED
            UtilizationEstimateStatus.GRANTED -> return next == UtilizationEstimateStatus.REVOKED || next == UtilizationEstimateStatus.EXPIRED
            UtilizationEstimateStatus.REVOKED -> return next == UtilizationEstimateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: UtilizationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("utilization estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedExposure(): Double = round((loadKg - attempts.toDouble()) * 100) / 100

    fun snapshot(): UtilizationEstimate {
        return UtilizationEstimate(
            code = code,
            name = name,
            title = title,
            depot = depot,
            checksum = checksum,
            tags = tags,
            attempts = attempts,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a utilization estimate in status requested. */
fun newUtilizationEstimate(code: String, name: String): UtilizationEstimate {
    return UtilizationEstimate(
        code = code,
        name = name,
        title = "",
        depot = "",
        checksum = "",
        tags = mutableListOf<String>(),
        attempts = 0,
        loadKg = 0.0,
        status = UtilizationEstimateStatus.REQUESTED,
    )
}
