package com.acme.transport.linehaul.capacity.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity quota records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity quota is in its life cycle. */
enum class CapacityQuotaStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capacity quota and its current status. */
data class CapacityQuota(
    var code: String,
    var title: String,
    var budget: Double,
    var segment: String,
    var contactEmail: String,
    var channel: String,
    var threshold: Double,
    var fuelLiters: Double,
    var status: CapacityQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityQuotaStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityQuotaStatus): Boolean {
        when (status) {
            CapacityQuotaStatus.PROPOSED -> {
                when (next) {
                    CapacityQuotaStatus.APPROVED, CapacityQuotaStatus.REJECTED -> return true
                }
                return false
            }
            CapacityQuotaStatus.APPROVED -> {
                when (next) {
                    CapacityQuotaStatus.REJECTED -> return true
                }
                return false
            }
            CapacityQuotaStatus.REJECTED -> {
                when (next) {
                    CapacityQuotaStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityQuotaStatus) {
        if (!canMoveTo(next)) {
            error("capacity quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capacity quota"} $code"

    fun effectiveVolume(): Double = round((threshold - budget) * 100) / 100

    fun snapshot(): CapacityQuota {
        return CapacityQuota(
            code = code,
            title = title,
            budget = budget,
            segment = segment,
            contactEmail = contactEmail,
            channel = channel,
            threshold = threshold,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capacity quota in status proposed. */
fun newCapacityQuota(code: String, title: String): CapacityQuota {
    return CapacityQuota(
        code = code,
        title = title,
        budget = 0.0,
        segment = "",
        contactEmail = "",
        channel = "",
        threshold = 0.0,
        fuelLiters = 0.0,
        status = CapacityQuotaStatus.PROPOSED,
    )
}
