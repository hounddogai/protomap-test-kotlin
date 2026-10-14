package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return profile is in its life cycle. */
enum class LeaseReturnProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One lease return profile, with what the planners need to act on it. */
data class LeaseReturnProfile(
    var code: String,
    var label: String,
    var capacity: Int,
    var ceiling: Double,
    var createdAt: Int,
    var visible: Boolean,
    var reviewer: String,
    var quantity: Int,
    var odometer: Int,
    var source: String,
    var status: LeaseReturnProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnProfileStatus): Boolean {
        when (status) {
            LeaseReturnProfileStatus.DRAFT -> {
                when (next) {
                    LeaseReturnProfileStatus.SUSPENDED, LeaseReturnProfileStatus.CLOSED -> return true
                }
                return false
            }
            LeaseReturnProfileStatus.PENDING -> {
                when (next) {
                    LeaseReturnProfileStatus.CLOSED -> return true
                }
                return false
            }
            LeaseReturnProfileStatus.ACTIVE -> {
                when (next) {
                    LeaseReturnProfileStatus.SUSPENDED, LeaseReturnProfileStatus.CLOSED -> return true
                }
                return false
            }
            LeaseReturnProfileStatus.SUSPENDED -> {
                when (next) {
                    LeaseReturnProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LeaseReturnProfileStatus) {
        if (!canMoveTo(next)) {
            error("lease return profile $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((quantity.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnProfile {
        return LeaseReturnProfile(
            code = code,
            label = label,
            capacity = capacity,
            ceiling = ceiling,
            createdAt = createdAt,
            visible = visible,
            reviewer = reviewer,
            quantity = quantity,
            odometer = odometer,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lease return profile in status draft. */
fun newLeaseReturnProfile(code: String, label: String): LeaseReturnProfile {
    return LeaseReturnProfile(
        code = code,
        label = label,
        capacity = 0,
        ceiling = 0.0,
        createdAt = 0,
        visible = false,
        reviewer = "",
        quantity = 0,
        odometer = 0,
        source = "",
        status = LeaseReturnProfileStatus.DRAFT,
    )
}
