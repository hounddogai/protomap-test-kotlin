package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered backhaul rollup is in its life cycle. */
enum class TieredBackhaulRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered backhaul rollup as the backhaul team keeps it. */
data class TieredBackhaulRollup(
    var code: String,
    var name: String,
    var score: Double,
    var vehicleId: String,
    var limit: Int,
    var country: String,
    var updatedAt: Int,
    var routeCode: String,
    var region: String,
    var ceiling: Double,
    var stopCount: Int,
    var status: TieredBackhaulRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBackhaulRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBackhaulRollupStatus): Boolean {
        when (status) {
            TieredBackhaulRollupStatus.PLANNED -> {
                when (next) {
                    TieredBackhaulRollupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredBackhaulRollupStatus.SCHEDULED -> {
                when (next) {
                    TieredBackhaulRollupStatus.IN_PROGRESS, TieredBackhaulRollupStatus.CANCELLED -> return true
                }
                return false
            }
            TieredBackhaulRollupStatus.IN_PROGRESS -> {
                when (next) {
                    TieredBackhaulRollupStatus.COMPLETED -> return true
                }
                return false
            }
            TieredBackhaulRollupStatus.COMPLETED -> {
                when (next) {
                    TieredBackhaulRollupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredBackhaulRollupStatus) {
        if (!canMoveTo(next)) {
            error("tiered backhaul rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered backhaul rollup"} $code"

    fun projectedLoad(): Double = round((updatedAt.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): TieredBackhaulRollup {
        return TieredBackhaulRollup(
            code = code,
            name = name,
            score = score,
            vehicleId = vehicleId,
            limit = limit,
            country = country,
            updatedAt = updatedAt,
            routeCode = routeCode,
            region = region,
            ceiling = ceiling,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered backhaul rollup in status planned. */
fun newTieredBackhaulRollup(code: String, name: String): TieredBackhaulRollup {
    return TieredBackhaulRollup(
        code = code,
        name = name,
        score = 0.0,
        vehicleId = "",
        limit = 0,
        country = "",
        updatedAt = 0,
        routeCode = "",
        region = "",
        ceiling = 0.0,
        stopCount = 0,
        status = TieredBackhaulRollupStatus.PLANNED,
    )
}
