package com.acme.transport.safety.incident.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident tier is in its life cycle. */
enum class IncidentTierStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A incident tier as the incident team keeps it. */
data class IncidentTier(
    var code: String,
    var label: String,
    var margin: Double,
    var ceiling: Double,
    var limit: Int,
    var unit: String,
    var startsAt: Int,
    var cost: Double,
    var status: IncidentTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentTierStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentTierStatus): Boolean {
        when (status) {
            IncidentTierStatus.OPEN -> {
                when (next) {
                    IncidentTierStatus.REOPENED -> return true
                }
                return false
            }
            IncidentTierStatus.IN_REVIEW -> {
                when (next) {
                    IncidentTierStatus.RESOLVED, IncidentTierStatus.ARCHIVED -> return true
                }
                return false
            }
            IncidentTierStatus.RESOLVED -> {
                when (next) {
                    IncidentTierStatus.REOPENED -> return true
                }
                return false
            }
            IncidentTierStatus.REOPENED -> {
                when (next) {
                    IncidentTierStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentTierStatus) {
        if (!canMoveTo(next)) {
            error("incident tier $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((cost - limit.toDouble()) * 100) / 100

    fun snapshot(): IncidentTier {
        return IncidentTier(
            code = code,
            label = label,
            margin = margin,
            ceiling = ceiling,
            limit = limit,
            unit = unit,
            startsAt = startsAt,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a incident tier in status open. */
fun newIncidentTier(code: String, label: String): IncidentTier {
    return IncidentTier(
        code = code,
        label = label,
        margin = 0.0,
        ceiling = 0.0,
        limit = 0,
        unit = "",
        startsAt = 0,
        cost = 0.0,
        status = IncidentTierStatus.OPEN,
    )
}
