package com.acme.transport.fleetops.leasereturn.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return manifest is in its life cycle. */
enum class LeaseReturnManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lease return manifest. Its code is unique within a tenant. */
data class LeaseReturnManifest(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var phone: String,
    var country: String,
    var rate: Double,
    var endsAt: Int,
    var carrier: String,
    var status: LeaseReturnManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnManifestStatus): Boolean {
        when (status) {
            LeaseReturnManifestStatus.PLANNED -> {
                when (next) {
                    LeaseReturnManifestStatus.IN_PROGRESS, LeaseReturnManifestStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnManifestStatus.SCHEDULED -> {
                when (next) {
                    LeaseReturnManifestStatus.COMPLETED -> return true
                }
                return false
            }
            LeaseReturnManifestStatus.IN_PROGRESS -> return next == LeaseReturnManifestStatus.COMPLETED || next == LeaseReturnManifestStatus.CANCELLED
            LeaseReturnManifestStatus.COMPLETED -> {
                when (next) {
                    LeaseReturnManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnManifestStatus) {
        if (!canMoveTo(next)) {
            error("lease return manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedExposure(): Double = round((fuelLiters - rate) * 100) / 100

    fun snapshot(): LeaseReturnManifest {
        return LeaseReturnManifest(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            phone = phone,
            country = country,
            rate = rate,
            endsAt = endsAt,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lease return manifest in status planned. */
fun newLeaseReturnManifest(code: String, label: String): LeaseReturnManifest {
    return LeaseReturnManifest(
        code = code,
        label = label,
        fuelLiters = 0.0,
        phone = "",
        country = "",
        rate = 0.0,
        endsAt = 0,
        carrier = "",
        status = LeaseReturnManifestStatus.PLANNED,
    )
}
