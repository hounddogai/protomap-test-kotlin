package com.acme.transport.crewing.dutyperiod.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period policy is in its life cycle. */
enum class DutyPeriodPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One duty period policy, with what the planners need to act on it. */
data class DutyPeriodPolicy(
    var code: String,
    var label: String,
    var reviewer: String,
    var pallets: Int,
    var retries: Int,
    var carrier: String,
    var externalRef: String,
    var floorValue: Double,
    var contactEmail: String,
    var durationMinutes: Int,
    var status: DutyPeriodPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodPolicyStatus): Boolean {
        when (status) {
            DutyPeriodPolicyStatus.DRAFT -> {
                when (next) {
                    DutyPeriodPolicyStatus.PENDING, DutyPeriodPolicyStatus.CLOSED -> return true
                }
                return false
            }
            DutyPeriodPolicyStatus.PENDING -> return next == DutyPeriodPolicyStatus.SUSPENDED
            DutyPeriodPolicyStatus.ACTIVE -> {
                when (next) {
                    DutyPeriodPolicyStatus.SUSPENDED, DutyPeriodPolicyStatus.CLOSED -> return true
                }
                return false
            }
            DutyPeriodPolicyStatus.SUSPENDED -> return next == DutyPeriodPolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: DutyPeriodPolicyStatus) {
        if (!canMoveTo(next)) {
            error("duty period policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedExposure(): Double = round(floorValue * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodPolicy {
        return DutyPeriodPolicy(
            code = code,
            label = label,
            reviewer = reviewer,
            pallets = pallets,
            retries = retries,
            carrier = carrier,
            externalRef = externalRef,
            floorValue = floorValue,
            contactEmail = contactEmail,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period policy in status draft. */
fun newDutyPeriodPolicy(code: String, label: String): DutyPeriodPolicy {
    return DutyPeriodPolicy(
        code = code,
        label = label,
        reviewer = "",
        pallets = 0,
        retries = 0,
        carrier = "",
        externalRef = "",
        floorValue = 0.0,
        contactEmail = "",
        durationMinutes = 0,
        status = DutyPeriodPolicyStatus.DRAFT,
    )
}
