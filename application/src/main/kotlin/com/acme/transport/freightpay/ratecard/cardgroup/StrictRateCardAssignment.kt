package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict rate card assignment is in its life cycle. */
enum class StrictRateCardAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a strict rate card assignment and its current status. */
data class StrictRateCardAssignment(
    var code: String,
    var title: String,
    var driverId: String,
    var createdAt: Int,
    var endsAt: Int,
    var floorValue: Double,
    var loadKg: Double,
    var ownerId: String,
    var windowDays: Int,
    var region: String,
    var status: StrictRateCardAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRateCardAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRateCardAssignmentStatus): Boolean {
        when (status) {
            StrictRateCardAssignmentStatus.PLANNED -> return next == StrictRateCardAssignmentStatus.SCHEDULED || next == StrictRateCardAssignmentStatus.CANCELLED
            StrictRateCardAssignmentStatus.SCHEDULED -> {
                when (next) {
                    StrictRateCardAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            StrictRateCardAssignmentStatus.IN_PROGRESS -> return next == StrictRateCardAssignmentStatus.COMPLETED || next == StrictRateCardAssignmentStatus.CANCELLED
            StrictRateCardAssignmentStatus.COMPLETED -> {
                when (next) {
                    StrictRateCardAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRateCardAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("strict rate card assignment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((floorValue + loadKg) * 100) / 100

    fun snapshot(): StrictRateCardAssignment {
        return StrictRateCardAssignment(
            code = code,
            title = title,
            driverId = driverId,
            createdAt = createdAt,
            endsAt = endsAt,
            floorValue = floorValue,
            loadKg = loadKg,
            ownerId = ownerId,
            windowDays = windowDays,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict rate card assignment in status planned. */
fun newStrictRateCardAssignment(code: String, title: String): StrictRateCardAssignment {
    return StrictRateCardAssignment(
        code = code,
        title = title,
        driverId = "",
        createdAt = 0,
        endsAt = 0,
        floorValue = 0.0,
        loadKg = 0.0,
        ownerId = "",
        windowDays = 0,
        region = "",
        status = StrictRateCardAssignmentStatus.PLANNED,
    )
}
