package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered ev charge plan is in its life cycle. */
enum class TieredEvChargePlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered ev charge plan. Its code is unique within a tenant. */
data class TieredEvChargePlan(
    var code: String,
    var title: String,
    var ownerId: String,
    var summary: String,
    var budget: Double,
    var reviewer: String,
    var name: String,
    var weight: Double,
    var archived: Boolean,
    var score: Double,
    var status: TieredEvChargePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredEvChargePlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredEvChargePlanStatus): Boolean {
        when (status) {
            TieredEvChargePlanStatus.CREATED -> return next == TieredEvChargePlanStatus.VALIDATED || next == TieredEvChargePlanStatus.PUBLISHED
            TieredEvChargePlanStatus.VALIDATED -> return next == TieredEvChargePlanStatus.RETIRED
            TieredEvChargePlanStatus.PUBLISHED -> {
                when (next) {
                    TieredEvChargePlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredEvChargePlanStatus) {
        if (!canMoveTo(next)) {
            error("tiered ev charge plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered ev charge plan"} $code"

    fun netTotal(): Double = round((score - budget) * 100) / 100

    fun snapshot(): TieredEvChargePlan {
        return TieredEvChargePlan(
            code = code,
            title = title,
            ownerId = ownerId,
            summary = summary,
            budget = budget,
            reviewer = reviewer,
            name = name,
            weight = weight,
            archived = archived,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered ev charge plan in status created. */
fun newTieredEvChargePlan(code: String, title: String): TieredEvChargePlan {
    return TieredEvChargePlan(
        code = code,
        title = title,
        ownerId = "",
        summary = "",
        budget = 0.0,
        reviewer = "",
        name = "",
        weight = 0.0,
        archived = false,
        score = 0.0,
        status = TieredEvChargePlanStatus.CREATED,
    )
}
