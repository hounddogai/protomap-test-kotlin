package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default cross dock snapshot records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a default cross dock snapshot is in its life cycle. */
enum class DefaultCrossDockSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default cross dock snapshot. Its code is unique within a tenant. */
data class DefaultCrossDockSnapshot(
    var code: String,
    var name: String,
    var retries: Int,
    var lane: String,
    var expiresAt: Int,
    var shiftCode: String,
    var notes: String,
    var budget: Double,
    var createdAt: Int,
    var sequence: Int,
    var phone: String,
    var status: DefaultCrossDockSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCrossDockSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCrossDockSnapshotStatus): Boolean {
        when (status) {
            DefaultCrossDockSnapshotStatus.REQUESTED -> return next == DefaultCrossDockSnapshotStatus.REVOKED
            DefaultCrossDockSnapshotStatus.GRANTED -> {
                when (next) {
                    DefaultCrossDockSnapshotStatus.REVOKED, DefaultCrossDockSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultCrossDockSnapshotStatus.REVOKED -> return next == DefaultCrossDockSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultCrossDockSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default cross dock snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default cross dock snapshot"} $code"

    fun weightedTotal(): Double = round((budget + retries.toDouble()) * 100) / 100

    fun snapshot(): DefaultCrossDockSnapshot {
        return DefaultCrossDockSnapshot(
            code = code,
            name = name,
            retries = retries,
            lane = lane,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            notes = notes,
            budget = budget,
            createdAt = createdAt,
            sequence = sequence,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a default cross dock snapshot in status requested. */
fun newDefaultCrossDockSnapshot(code: String, name: String): DefaultCrossDockSnapshot {
    return DefaultCrossDockSnapshot(
        code = code,
        name = name,
        retries = 0,
        lane = "",
        expiresAt = 0,
        shiftCode = "",
        notes = "",
        budget = 0.0,
        createdAt = 0,
        sequence = 0,
        phone = "",
        status = DefaultCrossDockSnapshotStatus.REQUESTED,
    )
}
