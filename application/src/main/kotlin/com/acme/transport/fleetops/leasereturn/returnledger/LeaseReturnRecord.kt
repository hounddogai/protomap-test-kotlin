package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return record is in its life cycle. */
enum class LeaseReturnRecordStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lease return record as the lease return team keeps it. */
data class LeaseReturnRecord(
    var code: String,
    var title: String,
    var notes: String,
    var budget: Double,
    var weight: Double,
    var pinned: Boolean,
    var country: String,
    var status: LeaseReturnRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnRecordStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnRecordStatus): Boolean {
        when (status) {
            LeaseReturnRecordStatus.REQUESTED -> {
                when (next) {
                    LeaseReturnRecordStatus.REVOKED -> return true
                }
                return false
            }
            LeaseReturnRecordStatus.GRANTED -> {
                when (next) {
                    LeaseReturnRecordStatus.REVOKED -> return true
                }
                return false
            }
            LeaseReturnRecordStatus.REVOKED -> return next == LeaseReturnRecordStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LeaseReturnRecordStatus) {
        if (!canMoveTo(next)) {
            error("lease return record $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round((weight - budget) * 100) / 100

    fun snapshot(): LeaseReturnRecord {
        return LeaseReturnRecord(
            code = code,
            title = title,
            notes = notes,
            budget = budget,
            weight = weight,
            pinned = pinned,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lease return record in status requested. */
fun newLeaseReturnRecord(code: String, title: String): LeaseReturnRecord {
    return LeaseReturnRecord(
        code = code,
        title = title,
        notes = "",
        budget = 0.0,
        weight = 0.0,
        pinned = false,
        country = "",
        status = LeaseReturnRecordStatus.REQUESTED,
    )
}
