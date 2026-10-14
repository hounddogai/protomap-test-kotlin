package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return preference records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a lease return preference is in its life cycle. */
enum class LeaseReturnPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lease return preference as the lease return team keeps it. */
data class LeaseReturnPreference(
    var code: String,
    var name: String,
    var endsAt: Int,
    var unit: String,
    var stopCount: Int,
    var etaMinutes: Int,
    var region: String,
    var title: String,
    var fuelLiters: Double,
    var status: LeaseReturnPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnPreferenceStatus): Boolean {
        when (status) {
            LeaseReturnPreferenceStatus.REQUESTED -> {
                when (next) {
                    LeaseReturnPreferenceStatus.GRANTED, LeaseReturnPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            LeaseReturnPreferenceStatus.GRANTED -> {
                when (next) {
                    LeaseReturnPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            LeaseReturnPreferenceStatus.REVOKED -> {
                when (next) {
                    LeaseReturnPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("lease return preference $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round((endsAt.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnPreference {
        return LeaseReturnPreference(
            code = code,
            name = name,
            endsAt = endsAt,
            unit = unit,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            region = region,
            title = title,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lease return preference in status requested. */
fun newLeaseReturnPreference(code: String, name: String): LeaseReturnPreference {
    return LeaseReturnPreference(
        code = code,
        name = name,
        endsAt = 0,
        unit = "",
        stopCount = 0,
        etaMinutes = 0,
        region = "",
        title = "",
        fuelLiters = 0.0,
        status = LeaseReturnPreferenceStatus.REQUESTED,
    )
}
