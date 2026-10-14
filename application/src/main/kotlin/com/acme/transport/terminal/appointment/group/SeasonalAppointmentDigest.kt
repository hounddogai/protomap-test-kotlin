package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal appointment digest is in its life cycle. */
enum class SeasonalAppointmentDigestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal appointment digest as the appointment team keeps it. */
data class SeasonalAppointmentDigest(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var category: String,
    var discount: Double,
    var ceiling: Double,
    var version: Int,
    var visible: Boolean,
    var summary: String,
    var timezone: String,
    var routeCode: String,
    var status: SeasonalAppointmentDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAppointmentDigestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAppointmentDigestStatus): Boolean {
        when (status) {
            SeasonalAppointmentDigestStatus.PROPOSED -> {
                when (next) {
                    SeasonalAppointmentDigestStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalAppointmentDigestStatus.APPROVED -> return next == SeasonalAppointmentDigestStatus.WITHDRAWN
            SeasonalAppointmentDigestStatus.REJECTED -> {
                when (next) {
                    SeasonalAppointmentDigestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAppointmentDigestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal appointment digest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedVolume(): Double = round((ceiling - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalAppointmentDigest {
        return SeasonalAppointmentDigest(
            code = code,
            name = name,
            updatedAt = updatedAt,
            category = category,
            discount = discount,
            ceiling = ceiling,
            version = version,
            visible = visible,
            summary = summary,
            timezone = timezone,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal appointment digest in status proposed. */
fun newSeasonalAppointmentDigest(code: String, name: String): SeasonalAppointmentDigest {
    return SeasonalAppointmentDigest(
        code = code,
        name = name,
        updatedAt = 0,
        category = "",
        discount = 0.0,
        ceiling = 0.0,
        version = 0,
        visible = false,
        summary = "",
        timezone = "",
        routeCode = "",
        status = SeasonalAppointmentDigestStatus.PROPOSED,
    )
}
