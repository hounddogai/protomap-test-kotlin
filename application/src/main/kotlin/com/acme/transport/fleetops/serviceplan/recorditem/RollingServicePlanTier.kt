package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling service plan tier is in its life cycle. */
enum class RollingServicePlanTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling service plan tier and its current status. */
data class RollingServicePlanTier(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var minimum: Double,
    var updatedAt: Int,
    var weight: Double,
    var loadKg: Double,
    var version: Int,
    var title: String,
    var attempts: Int,
    var status: RollingServicePlanTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingServicePlanTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingServicePlanTierStatus): Boolean {
        when (status) {
            RollingServicePlanTierStatus.IDLE -> {
                when (next) {
                    RollingServicePlanTierStatus.STOPPED -> return true
                }
                return false
            }
            RollingServicePlanTierStatus.WARMING -> {
                when (next) {
                    RollingServicePlanTierStatus.READY, RollingServicePlanTierStatus.DRAINING -> return true
                }
                return false
            }
            RollingServicePlanTierStatus.READY -> return next == RollingServicePlanTierStatus.DRAINING
            RollingServicePlanTierStatus.DRAINING -> return next == RollingServicePlanTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingServicePlanTierStatus) {
        if (!canMoveTo(next)) {
            error("rolling service plan tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round((loadKg - weight) * 100) / 100

    fun snapshot(): RollingServicePlanTier {
        return RollingServicePlanTier(
            code = code,
            label = label,
            tags = tags,
            minimum = minimum,
            updatedAt = updatedAt,
            weight = weight,
            loadKg = loadKg,
            version = version,
            title = title,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling service plan tier in status idle. */
fun newRollingServicePlanTier(code: String, label: String): RollingServicePlanTier {
    return RollingServicePlanTier(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        updatedAt = 0,
        weight = 0.0,
        loadKg = 0.0,
        version = 0,
        title = "",
        attempts = 0,
        status = RollingServicePlanTierStatus.IDLE,
    )
}
