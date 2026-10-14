package com.acme.transport.customs.inspection.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection group is in its life cycle. */
enum class InspectionGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A inspection group. Its code is unique within a tenant. */
data class InspectionGroup(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var ratio: Double,
    var minimum: Double,
    var status: InspectionGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionGroupStatus): Boolean {
        when (status) {
            InspectionGroupStatus.REQUESTED -> return next == InspectionGroupStatus.GRANTED
            InspectionGroupStatus.GRANTED -> {
                when (next) {
                    InspectionGroupStatus.REVOKED -> return true
                }
                return false
            }
            InspectionGroupStatus.REVOKED -> return next == InspectionGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: InspectionGroupStatus) {
        if (!canMoveTo(next)) {
            error("inspection group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((ratio + minimum) * 100) / 100

    fun snapshot(): InspectionGroup {
        return InspectionGroup(
            code = code,
            label = label,
            pinned = pinned,
            ratio = ratio,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection group in status requested. */
fun newInspectionGroup(code: String, label: String): InspectionGroup {
    return InspectionGroup(
        code = code,
        label = label,
        pinned = false,
        ratio = 0.0,
        minimum = 0.0,
        status = InspectionGroupStatus.REQUESTED,
    )
}
