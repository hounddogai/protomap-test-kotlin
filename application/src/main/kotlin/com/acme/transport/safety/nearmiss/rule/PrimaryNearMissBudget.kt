package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary near miss budget is in its life cycle. */
enum class PrimaryNearMissBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary near miss budget. Its code is unique within a tenant. */
data class PrimaryNearMissBudget(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var etaMinutes: Int,
    var timezone: String,
    var pallets: Int,
    var reviewer: String,
    var category: String,
    var source: String,
    var capacity: Int,
    var ratio: Double,
    var status: PrimaryNearMissBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryNearMissBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryNearMissBudgetStatus): Boolean {
        when (status) {
            PrimaryNearMissBudgetStatus.IDLE -> return next == PrimaryNearMissBudgetStatus.DRAINING
            PrimaryNearMissBudgetStatus.WARMING -> return next == PrimaryNearMissBudgetStatus.READY
            PrimaryNearMissBudgetStatus.READY -> return next == PrimaryNearMissBudgetStatus.DRAINING || next == PrimaryNearMissBudgetStatus.STOPPED
            PrimaryNearMissBudgetStatus.DRAINING -> {
                when (next) {
                    PrimaryNearMissBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryNearMissBudgetStatus) {
        if (!canMoveTo(next)) {
            error("primary near miss budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveValue(): Double = round(pallets.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): PrimaryNearMissBudget {
        return PrimaryNearMissBudget(
            code = code,
            title = title,
            distanceKm = distanceKm,
            etaMinutes = etaMinutes,
            timezone = timezone,
            pallets = pallets,
            reviewer = reviewer,
            category = category,
            source = source,
            capacity = capacity,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary near miss budget in status idle. */
fun newPrimaryNearMissBudget(code: String, title: String): PrimaryNearMissBudget {
    return PrimaryNearMissBudget(
        code = code,
        title = title,
        distanceKm = 0.0,
        etaMinutes = 0,
        timezone = "",
        pallets = 0,
        reviewer = "",
        category = "",
        source = "",
        capacity = 0,
        ratio = 0.0,
        status = PrimaryNearMissBudgetStatus.IDLE,
    )
}
