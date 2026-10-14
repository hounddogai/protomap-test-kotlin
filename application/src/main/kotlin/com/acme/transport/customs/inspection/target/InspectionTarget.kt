package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection target records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection target is in its life cycle. */
enum class InspectionTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A inspection target. Its code is unique within a tenant. */
data class InspectionTarget(
    var code: String,
    var title: String,
    var ceiling: Double,
    var carrier: String,
    var weight: Double,
    var status: InspectionTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionTargetStatus): Boolean {
        when (status) {
            InspectionTargetStatus.PROPOSED -> return next == InspectionTargetStatus.APPROVED
            InspectionTargetStatus.APPROVED -> {
                when (next) {
                    InspectionTargetStatus.REJECTED -> return true
                }
                return false
            }
            InspectionTargetStatus.REJECTED -> return next == InspectionTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: InspectionTargetStatus) {
        if (!canMoveTo(next)) {
            error("inspection target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun effectiveTotal(): Double = round(ceiling * weight * 100) / 100

    fun snapshot(): InspectionTarget {
        return InspectionTarget(
            code = code,
            title = title,
            ceiling = ceiling,
            carrier = carrier,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection target in status proposed. */
fun newInspectionTarget(code: String, title: String): InspectionTarget {
    return InspectionTarget(
        code = code,
        title = title,
        ceiling = 0.0,
        carrier = "",
        weight = 0.0,
        status = InspectionTargetStatus.PROPOSED,
    )
}
