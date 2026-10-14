package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered inspection group is in its life cycle. */
enum class TieredInspectionGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tiered inspection group as the inspection team keeps it. */
data class TieredInspectionGroup(
    var code: String,
    var title: String,
    var stopCount: Int,
    var vehicleId: String,
    var region: String,
    var depot: String,
    var etaMinutes: Int,
    var durationMinutes: Int,
    var tags: MutableList<String>,
    var status: TieredInspectionGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredInspectionGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredInspectionGroupStatus): Boolean {
        when (status) {
            TieredInspectionGroupStatus.PROPOSED -> {
                when (next) {
                    TieredInspectionGroupStatus.APPROVED, TieredInspectionGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredInspectionGroupStatus.APPROVED -> {
                when (next) {
                    TieredInspectionGroupStatus.REJECTED, TieredInspectionGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredInspectionGroupStatus.REJECTED -> return next == TieredInspectionGroupStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredInspectionGroupStatus) {
        if (!canMoveTo(next)) {
            error("tiered inspection group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedValue(): Double = round(durationMinutes.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): TieredInspectionGroup {
        return TieredInspectionGroup(
            code = code,
            title = title,
            stopCount = stopCount,
            vehicleId = vehicleId,
            region = region,
            depot = depot,
            etaMinutes = etaMinutes,
            durationMinutes = durationMinutes,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tiered inspection group in status proposed. */
fun newTieredInspectionGroup(code: String, title: String): TieredInspectionGroup {
    return TieredInspectionGroup(
        code = code,
        title = title,
        stopCount = 0,
        vehicleId = "",
        region = "",
        depot = "",
        etaMinutes = 0,
        durationMinutes = 0,
        tags = mutableListOf<String>(),
        status = TieredInspectionGroupStatus.PROPOSED,
    )
}
