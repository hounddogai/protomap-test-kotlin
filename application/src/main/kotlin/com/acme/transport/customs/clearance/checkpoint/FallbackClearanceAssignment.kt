package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback clearance assignment is in its life cycle. */
enum class FallbackClearanceAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback clearance assignment as the clearance team keeps it. */
data class FallbackClearanceAssignment(
    var code: String,
    var label: String,
    var discount: Double,
    var etaMinutes: Int,
    var notes: String,
    var limit: Int,
    var timezone: String,
    var threshold: Double,
    var vehicleId: String,
    var endsAt: Int,
    var cost: Double,
    var status: FallbackClearanceAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackClearanceAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackClearanceAssignmentStatus): Boolean {
        when (status) {
            FallbackClearanceAssignmentStatus.CREATED -> {
                when (next) {
                    FallbackClearanceAssignmentStatus.VALIDATED, FallbackClearanceAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            FallbackClearanceAssignmentStatus.VALIDATED -> return next == FallbackClearanceAssignmentStatus.PUBLISHED
            FallbackClearanceAssignmentStatus.PUBLISHED -> return next == FallbackClearanceAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackClearanceAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback clearance assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round((limit.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackClearanceAssignment {
        return FallbackClearanceAssignment(
            code = code,
            label = label,
            discount = discount,
            etaMinutes = etaMinutes,
            notes = notes,
            limit = limit,
            timezone = timezone,
            threshold = threshold,
            vehicleId = vehicleId,
            endsAt = endsAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback clearance assignment in status created. */
fun newFallbackClearanceAssignment(code: String, label: String): FallbackClearanceAssignment {
    return FallbackClearanceAssignment(
        code = code,
        label = label,
        discount = 0.0,
        etaMinutes = 0,
        notes = "",
        limit = 0,
        timezone = "",
        threshold = 0.0,
        vehicleId = "",
        endsAt = 0,
        cost = 0.0,
        status = FallbackClearanceAssignmentStatus.CREATED,
    )
}
