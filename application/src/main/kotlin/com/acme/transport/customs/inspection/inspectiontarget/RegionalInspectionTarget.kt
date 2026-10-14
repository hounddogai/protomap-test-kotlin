package com.acme.transport.customs.inspection.inspectiontarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional inspection target records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a regional inspection target is in its life cycle. */
enum class RegionalInspectionTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A regional inspection target. Its code is unique within a tenant. */
data class RegionalInspectionTarget(
    var code: String,
    var name: String,
    var budget: Double,
    var endsAt: Int,
    var quantity: Int,
    var durationMinutes: Int,
    var unit: String,
    var status: RegionalInspectionTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalInspectionTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalInspectionTargetStatus): Boolean {
        when (status) {
            RegionalInspectionTargetStatus.REQUESTED -> return next == RegionalInspectionTargetStatus.REVOKED
            RegionalInspectionTargetStatus.GRANTED -> return next == RegionalInspectionTargetStatus.REVOKED
            RegionalInspectionTargetStatus.REVOKED -> return next == RegionalInspectionTargetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RegionalInspectionTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional inspection target $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((durationMinutes.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalInspectionTarget {
        return RegionalInspectionTarget(
            code = code,
            name = name,
            budget = budget,
            endsAt = endsAt,
            quantity = quantity,
            durationMinutes = durationMinutes,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a regional inspection target in status requested. */
fun newRegionalInspectionTarget(code: String, name: String): RegionalInspectionTarget {
    return RegionalInspectionTarget(
        code = code,
        name = name,
        budget = 0.0,
        endsAt = 0,
        quantity = 0,
        durationMinutes = 0,
        unit = "",
        status = RegionalInspectionTargetStatus.REQUESTED,
    )
}
