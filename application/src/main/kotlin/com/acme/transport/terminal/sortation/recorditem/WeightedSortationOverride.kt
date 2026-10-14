package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted sortation override is in its life cycle. */
enum class WeightedSortationOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted sortation override. Its code is unique within a tenant. */
data class WeightedSortationOverride(
    var code: String,
    var name: String,
    var vehicleId: String,
    var margin: Double,
    var currency: String,
    var title: String,
    var driverId: String,
    var createdAt: Int,
    var etaMinutes: Int,
    var checksum: String,
    var windowDays: Int,
    var status: WeightedSortationOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSortationOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSortationOverrideStatus): Boolean {
        when (status) {
            WeightedSortationOverrideStatus.PROPOSED -> {
                when (next) {
                    WeightedSortationOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedSortationOverrideStatus.APPROVED -> {
                when (next) {
                    WeightedSortationOverrideStatus.REJECTED -> return true
                }
                return false
            }
            WeightedSortationOverrideStatus.REJECTED -> return next == WeightedSortationOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedSortationOverrideStatus) {
        if (!canMoveTo(next)) {
            error("weighted sortation override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((etaMinutes.toDouble() + margin) * 100) / 100

    fun snapshot(): WeightedSortationOverride {
        return WeightedSortationOverride(
            code = code,
            name = name,
            vehicleId = vehicleId,
            margin = margin,
            currency = currency,
            title = title,
            driverId = driverId,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            checksum = checksum,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted sortation override in status proposed. */
fun newWeightedSortationOverride(code: String, name: String): WeightedSortationOverride {
    return WeightedSortationOverride(
        code = code,
        name = name,
        vehicleId = "",
        margin = 0.0,
        currency = "",
        title = "",
        driverId = "",
        createdAt = 0,
        etaMinutes = 0,
        checksum = "",
        windowDays = 0,
        status = WeightedSortationOverrideStatus.PROPOSED,
    )
}
