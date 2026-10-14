package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped incident tier is in its life cycle. */
enum class CappedIncidentTierStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped incident tier as the incident team keeps it. */
data class CappedIncidentTier(
    var code: String,
    var label: String,
    var checksum: String,
    var approved: Boolean,
    var limit: Int,
    var windowDays: Int,
    var archived: Boolean,
    var budget: Double,
    var quantity: Int,
    var status: CappedIncidentTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIncidentTierStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIncidentTierStatus): Boolean {
        when (status) {
            CappedIncidentTierStatus.PROPOSED -> {
                when (next) {
                    CappedIncidentTierStatus.APPROVED, CappedIncidentTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedIncidentTierStatus.APPROVED -> {
                when (next) {
                    CappedIncidentTierStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedIncidentTierStatus.REJECTED -> return next == CappedIncidentTierStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedIncidentTierStatus) {
        if (!canMoveTo(next)) {
            error("capped incident tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((windowDays.toDouble() + budget) * 100) / 100

    fun snapshot(): CappedIncidentTier {
        return CappedIncidentTier(
            code = code,
            label = label,
            checksum = checksum,
            approved = approved,
            limit = limit,
            windowDays = windowDays,
            archived = archived,
            budget = budget,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped incident tier in status proposed. */
fun newCappedIncidentTier(code: String, label: String): CappedIncidentTier {
    return CappedIncidentTier(
        code = code,
        label = label,
        checksum = "",
        approved = false,
        limit = 0,
        windowDays = 0,
        archived = false,
        budget = 0.0,
        quantity = 0,
        status = CappedIncidentTierStatus.PROPOSED,
    )
}
