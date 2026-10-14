package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary availability baseline is in its life cycle. */
enum class PrimaryAvailabilityBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A primary availability baseline. Its code is unique within a tenant. */
data class PrimaryAvailabilityBaseline(
    var code: String,
    var name: String,
    var margin: Double,
    var amount: Double,
    var discount: Double,
    var loadKg: Double,
    var unit: String,
    var revision: Int,
    var status: PrimaryAvailabilityBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAvailabilityBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAvailabilityBaselineStatus): Boolean {
        when (status) {
            PrimaryAvailabilityBaselineStatus.OPEN -> {
                when (next) {
                    PrimaryAvailabilityBaselineStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryAvailabilityBaselineStatus.IN_REVIEW -> return next == PrimaryAvailabilityBaselineStatus.ARCHIVED
            PrimaryAvailabilityBaselineStatus.RESOLVED -> {
                when (next) {
                    PrimaryAvailabilityBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryAvailabilityBaselineStatus.REOPENED -> {
                when (next) {
                    PrimaryAvailabilityBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryAvailabilityBaselineStatus) {
        if (!canMoveTo(next)) {
            error("primary availability baseline $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"primary availability baseline"} $code"

    fun projectedVolume(): Double = round((revision.toDouble() + discount) * 100) / 100

    fun snapshot(): PrimaryAvailabilityBaseline {
        return PrimaryAvailabilityBaseline(
            code = code,
            name = name,
            margin = margin,
            amount = amount,
            discount = discount,
            loadKg = loadKg,
            unit = unit,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a primary availability baseline in status open. */
fun newPrimaryAvailabilityBaseline(code: String, name: String): PrimaryAvailabilityBaseline {
    return PrimaryAvailabilityBaseline(
        code = code,
        name = name,
        margin = 0.0,
        amount = 0.0,
        discount = 0.0,
        loadKg = 0.0,
        unit = "",
        revision = 0,
        status = PrimaryAvailabilityBaselineStatus.OPEN,
    )
}
