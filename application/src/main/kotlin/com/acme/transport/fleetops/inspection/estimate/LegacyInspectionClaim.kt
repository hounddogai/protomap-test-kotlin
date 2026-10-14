package com.acme.transport.fleetops.inspection.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection claim is in its life cycle. */
enum class LegacyInspectionClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy inspection claim. Its code is unique within a tenant. */
data class LegacyInspectionClaim(
    var code: String,
    var title: String,
    var floorValue: Double,
    var startsAt: Int,
    var ratio: Double,
    var fuelLiters: Double,
    var tags: MutableList<String>,
    var status: LegacyInspectionClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionClaimStatus): Boolean {
        when (status) {
            LegacyInspectionClaimStatus.PLANNED -> {
                when (next) {
                    LegacyInspectionClaimStatus.SCHEDULED, LegacyInspectionClaimStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyInspectionClaimStatus.SCHEDULED -> return next == LegacyInspectionClaimStatus.CANCELLED
            LegacyInspectionClaimStatus.IN_PROGRESS -> {
                when (next) {
                    LegacyInspectionClaimStatus.COMPLETED, LegacyInspectionClaimStatus.CANCELLED -> return true
                }
                return false
            }
            LegacyInspectionClaimStatus.COMPLETED -> return next == LegacyInspectionClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyInspectionClaimStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(ratio * floorValue * 100) / 100

    fun snapshot(): LegacyInspectionClaim {
        return LegacyInspectionClaim(
            code = code,
            title = title,
            floorValue = floorValue,
            startsAt = startsAt,
            ratio = ratio,
            fuelLiters = fuelLiters,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy inspection claim in status planned. */
fun newLegacyInspectionClaim(code: String, title: String): LegacyInspectionClaim {
    return LegacyInspectionClaim(
        code = code,
        title = title,
        floorValue = 0.0,
        startsAt = 0,
        ratio = 0.0,
        fuelLiters = 0.0,
        tags = mutableListOf<String>(),
        status = LegacyInspectionClaimStatus.PLANNED,
    )
}
