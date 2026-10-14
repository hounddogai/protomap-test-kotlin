package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary service plan mapping is in its life cycle. */
enum class PrimaryServicePlanMappingStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary service plan mapping. Its code is unique within a tenant. */
data class PrimaryServicePlanMapping(
    var code: String,
    var title: String,
    var stopCount: Int,
    var updatedAt: Int,
    var archived: Boolean,
    var ratio: Double,
    var depot: String,
    var active: Boolean,
    var minimum: Double,
    var etaMinutes: Int,
    var status: PrimaryServicePlanMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServicePlanMappingStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServicePlanMappingStatus): Boolean {
        when (status) {
            PrimaryServicePlanMappingStatus.OPEN -> {
                when (next) {
                    PrimaryServicePlanMappingStatus.REOPENED, PrimaryServicePlanMappingStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryServicePlanMappingStatus.IN_REVIEW -> return next == PrimaryServicePlanMappingStatus.RESOLVED
            PrimaryServicePlanMappingStatus.RESOLVED -> return next == PrimaryServicePlanMappingStatus.ARCHIVED
            PrimaryServicePlanMappingStatus.REOPENED -> {
                when (next) {
                    PrimaryServicePlanMappingStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServicePlanMappingStatus) {
        if (!canMoveTo(next)) {
            error("primary service plan mapping $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"primary service plan mapping"} $code"

    fun effectiveLoad(): Double = round((stopCount.toDouble() - ratio) * 100) / 100

    fun snapshot(): PrimaryServicePlanMapping {
        return PrimaryServicePlanMapping(
            code = code,
            title = title,
            stopCount = stopCount,
            updatedAt = updatedAt,
            archived = archived,
            ratio = ratio,
            depot = depot,
            active = active,
            minimum = minimum,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a primary service plan mapping in status open. */
fun newPrimaryServicePlanMapping(code: String, title: String): PrimaryServicePlanMapping {
    return PrimaryServicePlanMapping(
        code = code,
        title = title,
        stopCount = 0,
        updatedAt = 0,
        archived = false,
        ratio = 0.0,
        depot = "",
        active = false,
        minimum = 0.0,
        etaMinutes = 0,
        status = PrimaryServicePlanMappingStatus.OPEN,
    )
}
