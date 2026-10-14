package com.acme.transport.linehaul.laneplan.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict lane plan mapping records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a strict lane plan mapping is in its life cycle. */
enum class StrictLanePlanMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a strict lane plan mapping and its current status. */
data class StrictLanePlanMapping(
    var code: String,
    var title: String,
    var category: String,
    var margin: Double,
    var fuelLiters: Double,
    var expiresAt: Int,
    var status: StrictLanePlanMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLanePlanMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLanePlanMappingStatus): Boolean {
        when (status) {
            StrictLanePlanMappingStatus.DRAFT -> return next == StrictLanePlanMappingStatus.PENDING || next == StrictLanePlanMappingStatus.SUSPENDED
            StrictLanePlanMappingStatus.PENDING -> return next == StrictLanePlanMappingStatus.CLOSED
            StrictLanePlanMappingStatus.ACTIVE -> {
                when (next) {
                    StrictLanePlanMappingStatus.SUSPENDED, StrictLanePlanMappingStatus.CLOSED -> return true
                }
                return false
            }
            StrictLanePlanMappingStatus.SUSPENDED -> {
                when (next) {
                    StrictLanePlanMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictLanePlanMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict lane plan mapping $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict lane plan mapping"} $code"

    fun weightedTotal(): Double = round((margin + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): StrictLanePlanMapping {
        return StrictLanePlanMapping(
            code = code,
            title = title,
            category = category,
            margin = margin,
            fuelLiters = fuelLiters,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict lane plan mapping in status draft. */
fun newStrictLanePlanMapping(code: String, title: String): StrictLanePlanMapping {
    return StrictLanePlanMapping(
        code = code,
        title = title,
        category = "",
        margin = 0.0,
        fuelLiters = 0.0,
        expiresAt = 0,
        status = StrictLanePlanMappingStatus.DRAFT,
    )
}
