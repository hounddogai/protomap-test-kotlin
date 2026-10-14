package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability policy is in its life cycle. */
enum class AvailabilityPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a availability policy and its current status. */
data class AvailabilityPolicy(
    var code: String,
    var title: String,
    var segment: String,
    var region: String,
    var sequence: Int,
    var margin: Double,
    var pinned: Boolean,
    var status: AvailabilityPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityPolicyStatus): Boolean {
        when (status) {
            AvailabilityPolicyStatus.REQUESTED -> return next == AvailabilityPolicyStatus.EXPIRED
            AvailabilityPolicyStatus.GRANTED -> return next == AvailabilityPolicyStatus.REVOKED || next == AvailabilityPolicyStatus.EXPIRED
            AvailabilityPolicyStatus.REVOKED -> {
                when (next) {
                    AvailabilityPolicyStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityPolicyStatus) {
        if (!canMoveTo(next)) {
            error("availability policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveExposure(): Double = round((margin + sequence.toDouble()) * 100) / 100

    fun snapshot(): AvailabilityPolicy {
        return AvailabilityPolicy(
            code = code,
            title = title,
            segment = segment,
            region = region,
            sequence = sequence,
            margin = margin,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a availability policy in status requested. */
fun newAvailabilityPolicy(code: String, title: String): AvailabilityPolicy {
    return AvailabilityPolicy(
        code = code,
        title = title,
        segment = "",
        region = "",
        sequence = 0,
        margin = 0.0,
        pinned = false,
        status = AvailabilityPolicyStatus.REQUESTED,
    )
}
