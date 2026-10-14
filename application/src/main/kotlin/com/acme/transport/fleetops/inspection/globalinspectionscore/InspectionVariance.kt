package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection variance is in its life cycle. */
enum class InspectionVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One inspection variance, with what the planners need to act on it. */
data class InspectionVariance(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var ownerId: String,
    var capacity: Int,
    var pallets: Int,
    var status: InspectionVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionVarianceStatus): Boolean {
        when (status) {
            InspectionVarianceStatus.OPEN -> return next == InspectionVarianceStatus.IN_REVIEW || next == InspectionVarianceStatus.REOPENED
            InspectionVarianceStatus.IN_REVIEW -> return next == InspectionVarianceStatus.RESOLVED
            InspectionVarianceStatus.RESOLVED -> {
                when (next) {
                    InspectionVarianceStatus.REOPENED -> return true
                }
                return false
            }
            InspectionVarianceStatus.REOPENED -> return next == InspectionVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: InspectionVarianceStatus) {
        if (!canMoveTo(next)) {
            error("inspection variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netVolume(): Double = round((capacity.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionVariance {
        return InspectionVariance(
            code = code,
            title = title,
            expiresAt = expiresAt,
            ownerId = ownerId,
            capacity = capacity,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection variance in status open. */
fun newInspectionVariance(code: String, title: String): InspectionVariance {
    return InspectionVariance(
        code = code,
        title = title,
        expiresAt = 0,
        ownerId = "",
        capacity = 0,
        pallets = 0,
        status = InspectionVarianceStatus.OPEN,
    )
}
