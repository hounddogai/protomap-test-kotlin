package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback redelivery snapshot records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a fallback redelivery snapshot is in its life cycle. */
enum class FallbackRedeliverySnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback redelivery snapshot. Its code is unique within a tenant. */
data class FallbackRedeliverySnapshot(
    var code: String,
    var name: String,
    var weight: Double,
    var externalRef: String,
    var sequence: Int,
    var summary: String,
    var contactEmail: String,
    var threshold: Double,
    var depot: String,
    var durationMinutes: Int,
    var quantity: Int,
    var status: FallbackRedeliverySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRedeliverySnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRedeliverySnapshotStatus): Boolean {
        when (status) {
            FallbackRedeliverySnapshotStatus.PROPOSED -> return next == FallbackRedeliverySnapshotStatus.APPROVED || next == FallbackRedeliverySnapshotStatus.REJECTED
            FallbackRedeliverySnapshotStatus.APPROVED -> return next == FallbackRedeliverySnapshotStatus.REJECTED || next == FallbackRedeliverySnapshotStatus.WITHDRAWN
            FallbackRedeliverySnapshotStatus.REJECTED -> return next == FallbackRedeliverySnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackRedeliverySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("fallback redelivery snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((sequence.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): FallbackRedeliverySnapshot {
        return FallbackRedeliverySnapshot(
            code = code,
            name = name,
            weight = weight,
            externalRef = externalRef,
            sequence = sequence,
            summary = summary,
            contactEmail = contactEmail,
            threshold = threshold,
            depot = depot,
            durationMinutes = durationMinutes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback redelivery snapshot in status proposed. */
fun newFallbackRedeliverySnapshot(code: String, name: String): FallbackRedeliverySnapshot {
    return FallbackRedeliverySnapshot(
        code = code,
        name = name,
        weight = 0.0,
        externalRef = "",
        sequence = 0,
        summary = "",
        contactEmail = "",
        threshold = 0.0,
        depot = "",
        durationMinutes = 0,
        quantity = 0,
        status = FallbackRedeliverySnapshotStatus.PROPOSED,
    )
}
