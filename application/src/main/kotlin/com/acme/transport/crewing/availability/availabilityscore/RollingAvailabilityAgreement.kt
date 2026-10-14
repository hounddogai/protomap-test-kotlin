package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling availability agreement is in its life cycle. */
enum class RollingAvailabilityAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rolling availability agreement as the availability team keeps it. */
data class RollingAvailabilityAgreement(
    var code: String,
    var title: String,
    var trailerId: String,
    var limit: Int,
    var cost: Double,
    var fuelLiters: Double,
    var priority: Int,
    var notes: String,
    var source: String,
    var status: RollingAvailabilityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAvailabilityAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAvailabilityAgreementStatus): Boolean {
        when (status) {
            RollingAvailabilityAgreementStatus.CREATED -> return next == RollingAvailabilityAgreementStatus.VALIDATED
            RollingAvailabilityAgreementStatus.VALIDATED -> {
                when (next) {
                    RollingAvailabilityAgreementStatus.PUBLISHED, RollingAvailabilityAgreementStatus.RETIRED -> return true
                }
                return false
            }
            RollingAvailabilityAgreementStatus.PUBLISHED -> return next == RollingAvailabilityAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RollingAvailabilityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("rolling availability agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((priority.toDouble() - cost) * 100) / 100

    fun snapshot(): RollingAvailabilityAgreement {
        return RollingAvailabilityAgreement(
            code = code,
            title = title,
            trailerId = trailerId,
            limit = limit,
            cost = cost,
            fuelLiters = fuelLiters,
            priority = priority,
            notes = notes,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a rolling availability agreement in status created. */
fun newRollingAvailabilityAgreement(code: String, title: String): RollingAvailabilityAgreement {
    return RollingAvailabilityAgreement(
        code = code,
        title = title,
        trailerId = "",
        limit = 0,
        cost = 0.0,
        fuelLiters = 0.0,
        priority = 0,
        notes = "",
        source = "",
        status = RollingAvailabilityAgreementStatus.CREATED,
    )
}
