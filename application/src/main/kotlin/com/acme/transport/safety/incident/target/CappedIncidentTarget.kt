package com.acme.transport.safety.incident.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped incident target records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a capped incident target is in its life cycle. */
enum class CappedIncidentTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped incident target, with what the planners need to act on it. */
data class CappedIncidentTarget(
    var code: String,
    var title: String,
    var rate: Double,
    var depot: String,
    var attempts: Int,
    var active: Boolean,
    var loadKg: Double,
    var unit: String,
    var quantity: Int,
    var status: CappedIncidentTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedIncidentTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedIncidentTargetStatus): Boolean {
        when (status) {
            CappedIncidentTargetStatus.CREATED -> {
                when (next) {
                    CappedIncidentTargetStatus.VALIDATED, CappedIncidentTargetStatus.RETIRED -> return true
                }
                return false
            }
            CappedIncidentTargetStatus.VALIDATED -> {
                when (next) {
                    CappedIncidentTargetStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedIncidentTargetStatus.PUBLISHED -> return next == CappedIncidentTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CappedIncidentTargetStatus) {
        if (!canMoveTo(next)) {
            error("capped incident target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveValue(): Double = round((quantity.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): CappedIncidentTarget {
        return CappedIncidentTarget(
            code = code,
            title = title,
            rate = rate,
            depot = depot,
            attempts = attempts,
            active = active,
            loadKg = loadKg,
            unit = unit,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped incident target in status created. */
fun newCappedIncidentTarget(code: String, title: String): CappedIncidentTarget {
    return CappedIncidentTarget(
        code = code,
        title = title,
        rate = 0.0,
        depot = "",
        attempts = 0,
        active = false,
        loadKg = 0.0,
        unit = "",
        quantity = 0,
        status = CappedIncidentTargetStatus.CREATED,
    )
}
