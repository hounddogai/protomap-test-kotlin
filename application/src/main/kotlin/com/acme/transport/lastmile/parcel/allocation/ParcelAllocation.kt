package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel allocation records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a parcel allocation is in its life cycle. */
enum class ParcelAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One parcel allocation, with what the planners need to act on it. */
data class ParcelAllocation(
    var code: String,
    var name: String,
    var approved: Boolean,
    var attempts: Int,
    var sequence: Int,
    var phone: String,
    var attributes: MutableMap<String, String>,
    var status: ParcelAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelAllocationStatus): Boolean {
        when (status) {
            ParcelAllocationStatus.DRAFT -> return next == ParcelAllocationStatus.SUSPENDED || next == ParcelAllocationStatus.CLOSED
            ParcelAllocationStatus.PENDING -> {
                when (next) {
                    ParcelAllocationStatus.CLOSED -> return true
                }
                return false
            }
            ParcelAllocationStatus.ACTIVE -> return next == ParcelAllocationStatus.SUSPENDED
            ParcelAllocationStatus.SUSPENDED -> {
                when (next) {
                    ParcelAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelAllocationStatus) {
        if (!canMoveTo(next)) {
            error("parcel allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round(sequence.toDouble() * attempts.toDouble() * 100) / 100

    fun snapshot(): ParcelAllocation {
        return ParcelAllocation(
            code = code,
            name = name,
            approved = approved,
            attempts = attempts,
            sequence = sequence,
            phone = phone,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel allocation in status draft. */
fun newParcelAllocation(code: String, name: String): ParcelAllocation {
    return ParcelAllocation(
        code = code,
        name = name,
        approved = false,
        attempts = 0,
        sequence = 0,
        phone = "",
        attributes = mutableMapOf<String, String>(),
        status = ParcelAllocationStatus.DRAFT,
    )
}
