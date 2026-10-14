package com.acme.transport.network.zone.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient zone assignment is in its life cycle. */
enum class LenientZoneAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient zone assignment, with what the planners need to act on it. */
data class LenientZoneAssignment(
    var code: String,
    var title: String,
    var weight: Double,
    var pinned: Boolean,
    var externalRef: String,
    var category: String,
    var country: String,
    var region: String,
    var fuelLiters: Double,
    var graceDays: Int,
    var status: LenientZoneAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientZoneAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientZoneAssignmentStatus): Boolean {
        when (status) {
            LenientZoneAssignmentStatus.CREATED -> return next == LenientZoneAssignmentStatus.VALIDATED || next == LenientZoneAssignmentStatus.RETIRED
            LenientZoneAssignmentStatus.VALIDATED -> return next == LenientZoneAssignmentStatus.PUBLISHED
            LenientZoneAssignmentStatus.PUBLISHED -> {
                when (next) {
                    LenientZoneAssignmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientZoneAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient zone assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round(graceDays.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): LenientZoneAssignment {
        return LenientZoneAssignment(
            code = code,
            title = title,
            weight = weight,
            pinned = pinned,
            externalRef = externalRef,
            category = category,
            country = country,
            region = region,
            fuelLiters = fuelLiters,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a lenient zone assignment in status created. */
fun newLenientZoneAssignment(code: String, title: String): LenientZoneAssignment {
    return LenientZoneAssignment(
        code = code,
        title = title,
        weight = 0.0,
        pinned = false,
        externalRef = "",
        category = "",
        country = "",
        region = "",
        fuelLiters = 0.0,
        graceDays = 0,
        status = LenientZoneAssignmentStatus.CREATED,
    )
}
