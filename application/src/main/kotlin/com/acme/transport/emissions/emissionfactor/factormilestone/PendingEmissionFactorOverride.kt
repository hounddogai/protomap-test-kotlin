package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending emission factor override is in its life cycle. */
enum class PendingEmissionFactorOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One pending emission factor override, with what the planners need to act on it. */
data class PendingEmissionFactorOverride(
    var code: String,
    var name: String,
    var driverId: String,
    var loadKg: Double,
    var windowDays: Int,
    var category: String,
    var status: PendingEmissionFactorOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingEmissionFactorOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingEmissionFactorOverrideStatus): Boolean {
        when (status) {
            PendingEmissionFactorOverrideStatus.OPEN -> return next == PendingEmissionFactorOverrideStatus.RESOLVED || next == PendingEmissionFactorOverrideStatus.ARCHIVED
            PendingEmissionFactorOverrideStatus.IN_REVIEW -> {
                when (next) {
                    PendingEmissionFactorOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingEmissionFactorOverrideStatus.RESOLVED -> return next == PendingEmissionFactorOverrideStatus.ARCHIVED
            PendingEmissionFactorOverrideStatus.REOPENED -> return next == PendingEmissionFactorOverrideStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingEmissionFactorOverrideStatus) {
        if (!canMoveTo(next)) {
            error("pending emission factor override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveTotal(): Double = round((loadKg - windowDays.toDouble()) * 100) / 100

    fun snapshot(): PendingEmissionFactorOverride {
        return PendingEmissionFactorOverride(
            code = code,
            name = name,
            driverId = driverId,
            loadKg = loadKg,
            windowDays = windowDays,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending emission factor override in status open. */
fun newPendingEmissionFactorOverride(code: String, name: String): PendingEmissionFactorOverride {
    return PendingEmissionFactorOverride(
        code = code,
        name = name,
        driverId = "",
        loadKg = 0.0,
        windowDays = 0,
        category = "",
        status = PendingEmissionFactorOverrideStatus.OPEN,
    )
}
