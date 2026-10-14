package com.acme.transport.safety.nearmiss.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss snapshot is in its life cycle. */
enum class NearMissSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A near miss snapshot. Its code is unique within a tenant. */
data class NearMissSnapshot(
    var code: String,
    var title: String,
    var channel: String,
    var pallets: Int,
    var threshold: Double,
    var quantity: Int,
    var pinned: Boolean,
    var country: String,
    var budget: Double,
    var cost: Double,
    var approved: Boolean,
    var status: NearMissSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissSnapshotStatus): Boolean {
        when (status) {
            NearMissSnapshotStatus.REQUESTED -> return next == NearMissSnapshotStatus.REVOKED
            NearMissSnapshotStatus.GRANTED -> return next == NearMissSnapshotStatus.REVOKED || next == NearMissSnapshotStatus.EXPIRED
            NearMissSnapshotStatus.REVOKED -> return next == NearMissSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: NearMissSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("near miss snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((threshold - pallets.toDouble()) * 100) / 100

    fun snapshot(): NearMissSnapshot {
        return NearMissSnapshot(
            code = code,
            title = title,
            channel = channel,
            pallets = pallets,
            threshold = threshold,
            quantity = quantity,
            pinned = pinned,
            country = country,
            budget = budget,
            cost = cost,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a near miss snapshot in status requested. */
fun newNearMissSnapshot(code: String, title: String): NearMissSnapshot {
    return NearMissSnapshot(
        code = code,
        title = title,
        channel = "",
        pallets = 0,
        threshold = 0.0,
        quantity = 0,
        pinned = false,
        country = "",
        budget = 0.0,
        cost = 0.0,
        approved = false,
        status = NearMissSnapshotStatus.REQUESTED,
    )
}
