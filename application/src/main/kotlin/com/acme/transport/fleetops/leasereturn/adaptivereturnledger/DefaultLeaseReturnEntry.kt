package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default lease return entry is in its life cycle. */
enum class DefaultLeaseReturnEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a default lease return entry and its current status. */
data class DefaultLeaseReturnEntry(
    var code: String,
    var name: String,
    var threshold: Double,
    var vehicleId: String,
    var notes: String,
    var loadKg: Double,
    var status: DefaultLeaseReturnEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLeaseReturnEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLeaseReturnEntryStatus): Boolean {
        when (status) {
            DefaultLeaseReturnEntryStatus.DRAFT -> return next == DefaultLeaseReturnEntryStatus.PENDING || next == DefaultLeaseReturnEntryStatus.CLOSED
            DefaultLeaseReturnEntryStatus.PENDING -> {
                when (next) {
                    DefaultLeaseReturnEntryStatus.SUSPENDED, DefaultLeaseReturnEntryStatus.CLOSED -> return true
                }
                return false
            }
            DefaultLeaseReturnEntryStatus.ACTIVE -> {
                when (next) {
                    DefaultLeaseReturnEntryStatus.SUSPENDED, DefaultLeaseReturnEntryStatus.CLOSED -> return true
                }
                return false
            }
            DefaultLeaseReturnEntryStatus.SUSPENDED -> {
                when (next) {
                    DefaultLeaseReturnEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultLeaseReturnEntryStatus) {
        if (!canMoveTo(next)) {
            error("default lease return entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default lease return entry"} $code"

    fun netLoad(): Double = round((loadKg - threshold) * 100) / 100

    fun snapshot(): DefaultLeaseReturnEntry {
        return DefaultLeaseReturnEntry(
            code = code,
            name = name,
            threshold = threshold,
            vehicleId = vehicleId,
            notes = notes,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default lease return entry in status draft. */
fun newDefaultLeaseReturnEntry(code: String, name: String): DefaultLeaseReturnEntry {
    return DefaultLeaseReturnEntry(
        code = code,
        name = name,
        threshold = 0.0,
        vehicleId = "",
        notes = "",
        loadKg = 0.0,
        status = DefaultLeaseReturnEntryStatus.DRAFT,
    )
}
