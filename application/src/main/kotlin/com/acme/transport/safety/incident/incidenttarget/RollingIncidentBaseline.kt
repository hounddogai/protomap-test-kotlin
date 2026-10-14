package com.acme.transport.safety.incident.incidenttarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling incident baseline is in its life cycle. */
enum class RollingIncidentBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling incident baseline and its current status. */
data class RollingIncidentBaseline(
    var code: String,
    var title: String,
    var name: String,
    var region: String,
    var phone: String,
    var startsAt: Int,
    var etaMinutes: Int,
    var loadKg: Double,
    var endsAt: Int,
    var unit: String,
    var status: RollingIncidentBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingIncidentBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingIncidentBaselineStatus): Boolean {
        when (status) {
            RollingIncidentBaselineStatus.DRAFT -> {
                when (next) {
                    RollingIncidentBaselineStatus.ACTIVE -> return true
                }
                return false
            }
            RollingIncidentBaselineStatus.PENDING -> return next == RollingIncidentBaselineStatus.ACTIVE
            RollingIncidentBaselineStatus.ACTIVE -> return next == RollingIncidentBaselineStatus.SUSPENDED
            RollingIncidentBaselineStatus.SUSPENDED -> return next == RollingIncidentBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingIncidentBaselineStatus) {
        if (!canMoveTo(next)) {
            error("rolling incident baseline $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((loadKg + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): RollingIncidentBaseline {
        return RollingIncidentBaseline(
            code = code,
            title = title,
            name = name,
            region = region,
            phone = phone,
            startsAt = startsAt,
            etaMinutes = etaMinutes,
            loadKg = loadKg,
            endsAt = endsAt,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling incident baseline in status draft. */
fun newRollingIncidentBaseline(code: String, title: String): RollingIncidentBaseline {
    return RollingIncidentBaseline(
        code = code,
        title = title,
        name = "",
        region = "",
        phone = "",
        startsAt = 0,
        etaMinutes = 0,
        loadKg = 0.0,
        endsAt = 0,
        unit = "",
        status = RollingIncidentBaselineStatus.DRAFT,
    )
}
