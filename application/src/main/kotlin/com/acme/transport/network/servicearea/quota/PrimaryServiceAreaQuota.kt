package com.acme.transport.network.servicearea.quota

import com.acme.transport.kit.text.normalize

// The primary service area quota records of network's service area work, and the rules, stores, and
// calculations around them.

/** Where a primary service area quota is in its life cycle. */
enum class PrimaryServiceAreaQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary service area quota, with what the planners need to act on it. */
data class PrimaryServiceAreaQuota(
    var code: String,
    var label: String,
    var notes: String,
    var segment: String,
    var sequence: Int,
    var carrier: String,
    var name: String,
    var vehicleId: String,
    var driverId: String,
    var currency: String,
    var status: PrimaryServiceAreaQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServiceAreaQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServiceAreaQuotaStatus): Boolean {
        when (status) {
            PrimaryServiceAreaQuotaStatus.OPEN -> {
                when (next) {
                    PrimaryServiceAreaQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            PrimaryServiceAreaQuotaStatus.IN_REVIEW -> return next == PrimaryServiceAreaQuotaStatus.RESOLVED
            PrimaryServiceAreaQuotaStatus.RESOLVED -> return next == PrimaryServiceAreaQuotaStatus.ARCHIVED
            PrimaryServiceAreaQuotaStatus.REOPENED -> return next == PrimaryServiceAreaQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryServiceAreaQuotaStatus) {
        if (!canMoveTo(next)) {
            error("primary service area quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary service area quota"} $code"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): PrimaryServiceAreaQuota {
        return PrimaryServiceAreaQuota(
            code = code,
            label = label,
            notes = notes,
            segment = segment,
            sequence = sequence,
            carrier = carrier,
            name = name,
            vehicleId = vehicleId,
            driverId = driverId,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary service area quota in status open. */
fun newPrimaryServiceAreaQuota(code: String, label: String): PrimaryServiceAreaQuota {
    return PrimaryServiceAreaQuota(
        code = code,
        label = label,
        notes = "",
        segment = "",
        sequence = 0,
        carrier = "",
        name = "",
        vehicleId = "",
        driverId = "",
        currency = "",
        status = PrimaryServiceAreaQuotaStatus.OPEN,
    )
}
