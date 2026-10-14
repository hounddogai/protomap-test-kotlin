package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered fuel burn plan is in its life cycle. */
enum class TieredFuelBurnPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered fuel burn plan. Its code is unique within a tenant. */
data class TieredFuelBurnPlan(
    var code: String,
    var title: String,
    var externalRef: String,
    var tags: MutableList<String>,
    var endsAt: Int,
    var distanceKm: Double,
    var label: String,
    var active: Boolean,
    var status: TieredFuelBurnPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFuelBurnPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFuelBurnPlanStatus): Boolean {
        when (status) {
            TieredFuelBurnPlanStatus.IDLE -> return next == TieredFuelBurnPlanStatus.DRAINING
            TieredFuelBurnPlanStatus.WARMING -> return next == TieredFuelBurnPlanStatus.DRAINING
            TieredFuelBurnPlanStatus.READY -> return next == TieredFuelBurnPlanStatus.DRAINING
            TieredFuelBurnPlanStatus.DRAINING -> {
                when (next) {
                    TieredFuelBurnPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredFuelBurnPlanStatus) {
        if (!canMoveTo(next)) {
            error("tiered fuel burn plan $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((endsAt.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): TieredFuelBurnPlan {
        return TieredFuelBurnPlan(
            code = code,
            title = title,
            externalRef = externalRef,
            tags = tags,
            endsAt = endsAt,
            distanceKm = distanceKm,
            label = label,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered fuel burn plan in status idle. */
fun newTieredFuelBurnPlan(code: String, title: String): TieredFuelBurnPlan {
    return TieredFuelBurnPlan(
        code = code,
        title = title,
        externalRef = "",
        tags = mutableListOf<String>(),
        endsAt = 0,
        distanceKm = 0.0,
        label = "",
        active = false,
        status = TieredFuelBurnPlanStatus.IDLE,
    )
}
