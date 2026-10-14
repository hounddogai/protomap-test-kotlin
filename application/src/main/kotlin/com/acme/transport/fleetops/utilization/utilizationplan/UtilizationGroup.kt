package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization group is in its life cycle. */
enum class UtilizationGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A utilization group. Its code is unique within a tenant. */
data class UtilizationGroup(
    var code: String,
    var title: String,
    var discount: Double,
    var reviewer: String,
    var trailerId: String,
    var pinned: Boolean,
    var updatedAt: Int,
    var currency: String,
    var score: Double,
    var status: UtilizationGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationGroupStatus): Boolean {
        when (status) {
            UtilizationGroupStatus.CREATED -> return next == UtilizationGroupStatus.VALIDATED || next == UtilizationGroupStatus.RETIRED
            UtilizationGroupStatus.VALIDATED -> return next == UtilizationGroupStatus.PUBLISHED || next == UtilizationGroupStatus.RETIRED
            UtilizationGroupStatus.PUBLISHED -> return next == UtilizationGroupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: UtilizationGroupStatus) {
        if (!canMoveTo(next)) {
            error("utilization group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round((score - discount) * 100) / 100

    fun snapshot(): UtilizationGroup {
        return UtilizationGroup(
            code = code,
            title = title,
            discount = discount,
            reviewer = reviewer,
            trailerId = trailerId,
            pinned = pinned,
            updatedAt = updatedAt,
            currency = currency,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a utilization group in status created. */
fun newUtilizationGroup(code: String, title: String): UtilizationGroup {
    return UtilizationGroup(
        code = code,
        title = title,
        discount = 0.0,
        reviewer = "",
        trailerId = "",
        pinned = false,
        updatedAt = 0,
        currency = "",
        score = 0.0,
        status = UtilizationGroupStatus.CREATED,
    )
}
