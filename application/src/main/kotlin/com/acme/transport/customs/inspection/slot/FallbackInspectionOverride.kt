package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback inspection override is in its life cycle. */
enum class FallbackInspectionOverrideStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback inspection override. Its code is unique within a tenant. */
data class FallbackInspectionOverride(
    var code: String,
    var title: String,
    var ownerId: String,
    var summary: String,
    var cost: Double,
    var notes: String,
    var version: Int,
    var quantity: Int,
    var status: FallbackInspectionOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackInspectionOverrideStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackInspectionOverrideStatus): Boolean {
        when (status) {
            FallbackInspectionOverrideStatus.PLANNED -> return next == FallbackInspectionOverrideStatus.SCHEDULED
            FallbackInspectionOverrideStatus.SCHEDULED -> return next == FallbackInspectionOverrideStatus.COMPLETED
            FallbackInspectionOverrideStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackInspectionOverrideStatus.COMPLETED -> return true
                }
                return false
            }
            FallbackInspectionOverrideStatus.COMPLETED -> return next == FallbackInspectionOverrideStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackInspectionOverrideStatus) {
        if (!canMoveTo(next)) {
            error("fallback inspection override $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"fallback inspection override"} $code"

    fun projectedLoad(): Double = round((cost - version.toDouble()) * 100) / 100

    fun snapshot(): FallbackInspectionOverride {
        return FallbackInspectionOverride(
            code = code,
            title = title,
            ownerId = ownerId,
            summary = summary,
            cost = cost,
            notes = notes,
            version = version,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback inspection override in status planned. */
fun newFallbackInspectionOverride(code: String, title: String): FallbackInspectionOverride {
    return FallbackInspectionOverride(
        code = code,
        title = title,
        ownerId = "",
        summary = "",
        cost = 0.0,
        notes = "",
        version = 0,
        quantity = 0,
        status = FallbackInspectionOverrideStatus.PLANNED,
    )
}
