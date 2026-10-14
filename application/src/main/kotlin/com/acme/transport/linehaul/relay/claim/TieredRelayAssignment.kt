package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered relay assignment is in its life cycle. */
enum class TieredRelayAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered relay assignment as the relay team keeps it. */
data class TieredRelayAssignment(
    var code: String,
    var name: String,
    var notes: String,
    var checksum: String,
    var discount: Double,
    var budget: Double,
    var contactEmail: String,
    var pinned: Boolean,
    var status: TieredRelayAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRelayAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRelayAssignmentStatus): Boolean {
        when (status) {
            TieredRelayAssignmentStatus.CREATED -> return next == TieredRelayAssignmentStatus.VALIDATED || next == TieredRelayAssignmentStatus.RETIRED
            TieredRelayAssignmentStatus.VALIDATED -> return next == TieredRelayAssignmentStatus.PUBLISHED
            TieredRelayAssignmentStatus.PUBLISHED -> return next == TieredRelayAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredRelayAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("tiered relay assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round((discount - budget) * 100) / 100

    fun snapshot(): TieredRelayAssignment {
        return TieredRelayAssignment(
            code = code,
            name = name,
            notes = notes,
            checksum = checksum,
            discount = discount,
            budget = budget,
            contactEmail = contactEmail,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered relay assignment in status created. */
fun newTieredRelayAssignment(code: String, name: String): TieredRelayAssignment {
    return TieredRelayAssignment(
        code = code,
        name = name,
        notes = "",
        checksum = "",
        discount = 0.0,
        budget = 0.0,
        contactEmail = "",
        pinned = false,
        status = TieredRelayAssignmentStatus.CREATED,
    )
}
