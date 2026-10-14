package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment claim is in its life cycle. */
enum class AppointmentClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a appointment claim and its current status. */
data class AppointmentClaim(
    var code: String,
    var title: String,
    var score: Double,
    var tags: MutableList<String>,
    var category: String,
    var approved: Boolean,
    var createdAt: Int,
    var etaMinutes: Int,
    var driverId: String,
    var vehicleId: String,
    var status: AppointmentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentClaimStatus): Boolean {
        when (status) {
            AppointmentClaimStatus.CREATED -> return next == AppointmentClaimStatus.VALIDATED
            AppointmentClaimStatus.VALIDATED -> return next == AppointmentClaimStatus.PUBLISHED || next == AppointmentClaimStatus.RETIRED
            AppointmentClaimStatus.PUBLISHED -> {
                when (next) {
                    AppointmentClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentClaimStatus) {
        if (!canMoveTo(next)) {
            error("appointment claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun adjustedTotal(): Double = round(etaMinutes.toDouble() * createdAt.toDouble() * 100) / 100

    fun snapshot(): AppointmentClaim {
        return AppointmentClaim(
            code = code,
            title = title,
            score = score,
            tags = tags,
            category = category,
            approved = approved,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            driverId = driverId,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a appointment claim in status created. */
fun newAppointmentClaim(code: String, title: String): AppointmentClaim {
    return AppointmentClaim(
        code = code,
        title = title,
        score = 0.0,
        tags = mutableListOf<String>(),
        category = "",
        approved = false,
        createdAt = 0,
        etaMinutes = 0,
        driverId = "",
        vehicleId = "",
        status = AppointmentClaimStatus.CREATED,
    )
}
