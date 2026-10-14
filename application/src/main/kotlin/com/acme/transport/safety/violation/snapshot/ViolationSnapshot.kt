package com.acme.transport.safety.violation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The violation snapshot records of safety's violation work, and the rules, stores, and calculations around
// them.

/** Where a violation snapshot is in its life cycle. */
enum class ViolationSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a violation snapshot and its current status. */
data class ViolationSnapshot(
    var code: String,
    var name: String,
    var currency: String,
    var contactEmail: String,
    var limit: Int,
    var source: String,
    var distanceKm: Double,
    var graceDays: Int,
    var tags: MutableList<String>,
    var notes: String,
    var externalRef: String,
    var status: ViolationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ViolationSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ViolationSnapshotStatus): Boolean {
        when (status) {
            ViolationSnapshotStatus.PLANNED -> return next == ViolationSnapshotStatus.SCHEDULED
            ViolationSnapshotStatus.SCHEDULED -> return next == ViolationSnapshotStatus.CANCELLED
            ViolationSnapshotStatus.IN_PROGRESS -> return next == ViolationSnapshotStatus.COMPLETED || next == ViolationSnapshotStatus.CANCELLED
            ViolationSnapshotStatus.COMPLETED -> return next == ViolationSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ViolationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("violation snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round(limit.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): ViolationSnapshot {
        return ViolationSnapshot(
            code = code,
            name = name,
            currency = currency,
            contactEmail = contactEmail,
            limit = limit,
            source = source,
            distanceKm = distanceKm,
            graceDays = graceDays,
            tags = tags,
            notes = notes,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a violation snapshot in status planned. */
fun newViolationSnapshot(code: String, name: String): ViolationSnapshot {
    return ViolationSnapshot(
        code = code,
        name = name,
        currency = "",
        contactEmail = "",
        limit = 0,
        source = "",
        distanceKm = 0.0,
        graceDays = 0,
        tags = mutableListOf<String>(),
        notes = "",
        externalRef = "",
        status = ViolationSnapshotStatus.PLANNED,
    )
}
