package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return entry is in its life cycle. */
enum class LeaseReturnEntryStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lease return entry and its current status. */
data class LeaseReturnEntry(
    var code: String,
    var title: String,
    var priority: Int,
    var distanceKm: Double,
    var attempts: Int,
    var description: String,
    var approved: Boolean,
    var graceDays: Int,
    var channel: String,
    var weight: Double,
    var limit: Int,
    var status: LeaseReturnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnEntryStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnEntryStatus): Boolean {
        when (status) {
            LeaseReturnEntryStatus.REQUESTED -> {
                when (next) {
                    LeaseReturnEntryStatus.EXPIRED -> return true
                }
                return false
            }
            LeaseReturnEntryStatus.GRANTED -> return next == LeaseReturnEntryStatus.REVOKED
            LeaseReturnEntryStatus.REVOKED -> return next == LeaseReturnEntryStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LeaseReturnEntryStatus) {
        if (!canMoveTo(next)) {
            error("lease return entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((limit.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): LeaseReturnEntry {
        return LeaseReturnEntry(
            code = code,
            title = title,
            priority = priority,
            distanceKm = distanceKm,
            attempts = attempts,
            description = description,
            approved = approved,
            graceDays = graceDays,
            channel = channel,
            weight = weight,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a lease return entry in status requested. */
fun newLeaseReturnEntry(code: String, title: String): LeaseReturnEntry {
    return LeaseReturnEntry(
        code = code,
        title = title,
        priority = 0,
        distanceKm = 0.0,
        attempts = 0,
        description = "",
        approved = false,
        graceDays = 0,
        channel = "",
        weight = 0.0,
        limit = 0,
        status = LeaseReturnEntryStatus.REQUESTED,
    )
}
