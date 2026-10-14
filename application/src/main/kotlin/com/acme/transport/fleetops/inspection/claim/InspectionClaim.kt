package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection claim records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection claim is in its life cycle. */
enum class InspectionClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One inspection claim, with what the planners need to act on it. */
data class InspectionClaim(
    var code: String,
    var title: String,
    var driverId: String,
    var durationMinutes: Int,
    var notes: String,
    var unit: String,
    var threshold: Double,
    var depot: String,
    var revision: Int,
    var tags: MutableList<String>,
    var status: InspectionClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionClaimStatus): Boolean {
        when (status) {
            InspectionClaimStatus.DRAFT -> {
                when (next) {
                    InspectionClaimStatus.ACTIVE -> return true
                }
                return false
            }
            InspectionClaimStatus.PENDING -> return next == InspectionClaimStatus.SUSPENDED
            InspectionClaimStatus.ACTIVE -> {
                when (next) {
                    InspectionClaimStatus.SUSPENDED, InspectionClaimStatus.CLOSED -> return true
                }
                return false
            }
            InspectionClaimStatus.SUSPENDED -> return next == InspectionClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionClaimStatus) {
        if (!canMoveTo(next)) {
            error("inspection claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"inspection claim"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round(revision.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): InspectionClaim {
        return InspectionClaim(
            code = code,
            title = title,
            driverId = driverId,
            durationMinutes = durationMinutes,
            notes = notes,
            unit = unit,
            threshold = threshold,
            depot = depot,
            revision = revision,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a inspection claim in status draft. */
fun newInspectionClaim(code: String, title: String): InspectionClaim {
    return InspectionClaim(
        code = code,
        title = title,
        driverId = "",
        durationMinutes = 0,
        notes = "",
        unit = "",
        threshold = 0.0,
        depot = "",
        revision = 0,
        tags = mutableListOf<String>(),
        status = InspectionClaimStatus.DRAFT,
    )
}
