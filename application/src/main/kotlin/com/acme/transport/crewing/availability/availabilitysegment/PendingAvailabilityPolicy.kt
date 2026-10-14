package com.acme.transport.crewing.availability.availabilitysegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending availability policy is in its life cycle. */
enum class PendingAvailabilityPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a pending availability policy and its current status. */
data class PendingAvailabilityPolicy(
    var code: String,
    var label: String,
    var weight: Double,
    var ratio: Double,
    var budget: Double,
    var active: Boolean,
    var archived: Boolean,
    var rate: Double,
    var minimum: Double,
    var floorValue: Double,
    var status: PendingAvailabilityPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAvailabilityPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAvailabilityPolicyStatus): Boolean {
        when (status) {
            PendingAvailabilityPolicyStatus.CREATED -> {
                when (next) {
                    PendingAvailabilityPolicyStatus.VALIDATED, PendingAvailabilityPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingAvailabilityPolicyStatus.VALIDATED -> {
                when (next) {
                    PendingAvailabilityPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingAvailabilityPolicyStatus.PUBLISHED -> return next == PendingAvailabilityPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingAvailabilityPolicyStatus) {
        if (!canMoveTo(next)) {
            error("pending availability policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveExposure(): Double = round(weight * minimum * 100) / 100

    fun snapshot(): PendingAvailabilityPolicy {
        return PendingAvailabilityPolicy(
            code = code,
            label = label,
            weight = weight,
            ratio = ratio,
            budget = budget,
            active = active,
            archived = archived,
            rate = rate,
            minimum = minimum,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending availability policy in status created. */
fun newPendingAvailabilityPolicy(code: String, label: String): PendingAvailabilityPolicy {
    return PendingAvailabilityPolicy(
        code = code,
        label = label,
        weight = 0.0,
        ratio = 0.0,
        budget = 0.0,
        active = false,
        archived = false,
        rate = 0.0,
        minimum = 0.0,
        floorValue = 0.0,
        status = PendingAvailabilityPolicyStatus.CREATED,
    )
}
