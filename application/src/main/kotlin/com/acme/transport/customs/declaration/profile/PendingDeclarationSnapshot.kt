package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending declaration snapshot is in its life cycle. */
enum class PendingDeclarationSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending declaration snapshot. Its code is unique within a tenant. */
data class PendingDeclarationSnapshot(
    var code: String,
    var title: String,
    var driverId: String,
    var attributes: MutableMap<String, String>,
    var loadKg: Double,
    var timezone: String,
    var cost: Double,
    var description: String,
    var status: PendingDeclarationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingDeclarationSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingDeclarationSnapshotStatus): Boolean {
        when (status) {
            PendingDeclarationSnapshotStatus.OPEN -> return next == PendingDeclarationSnapshotStatus.ARCHIVED
            PendingDeclarationSnapshotStatus.IN_REVIEW -> {
                when (next) {
                    PendingDeclarationSnapshotStatus.REOPENED -> return true
                }
                return false
            }
            PendingDeclarationSnapshotStatus.RESOLVED -> {
                when (next) {
                    PendingDeclarationSnapshotStatus.REOPENED -> return true
                }
                return false
            }
            PendingDeclarationSnapshotStatus.REOPENED -> {
                when (next) {
                    PendingDeclarationSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingDeclarationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("pending declaration snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending declaration snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveTotal(): Double = round((loadKg - cost) * 100) / 100

    fun snapshot(): PendingDeclarationSnapshot {
        return PendingDeclarationSnapshot(
            code = code,
            title = title,
            driverId = driverId,
            attributes = attributes,
            loadKg = loadKg,
            timezone = timezone,
            cost = cost,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending declaration snapshot in status open. */
fun newPendingDeclarationSnapshot(code: String, title: String): PendingDeclarationSnapshot {
    return PendingDeclarationSnapshot(
        code = code,
        title = title,
        driverId = "",
        attributes = mutableMapOf<String, String>(),
        loadKg = 0.0,
        timezone = "",
        cost = 0.0,
        description = "",
        status = PendingDeclarationSnapshotStatus.OPEN,
    )
}
