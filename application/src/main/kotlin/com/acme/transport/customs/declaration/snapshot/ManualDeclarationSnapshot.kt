package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.kit.text.normalize

// The manual declaration snapshot records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a manual declaration snapshot is in its life cycle. */
enum class ManualDeclarationSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One manual declaration snapshot, with what the planners need to act on it. */
data class ManualDeclarationSnapshot(
    var code: String,
    var name: String,
    var archived: Boolean,
    var category: String,
    var visible: Boolean,
    var vehicleId: String,
    var score: Double,
    var status: ManualDeclarationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDeclarationSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDeclarationSnapshotStatus): Boolean {
        when (status) {
            ManualDeclarationSnapshotStatus.PROPOSED -> {
                when (next) {
                    ManualDeclarationSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            ManualDeclarationSnapshotStatus.APPROVED -> return next == ManualDeclarationSnapshotStatus.WITHDRAWN
            ManualDeclarationSnapshotStatus.REJECTED -> {
                when (next) {
                    ManualDeclarationSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDeclarationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("manual declaration snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = score

    fun snapshot(): ManualDeclarationSnapshot {
        return ManualDeclarationSnapshot(
            code = code,
            name = name,
            archived = archived,
            category = category,
            visible = visible,
            vehicleId = vehicleId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual declaration snapshot in status proposed. */
fun newManualDeclarationSnapshot(code: String, name: String): ManualDeclarationSnapshot {
    return ManualDeclarationSnapshot(
        code = code,
        name = name,
        archived = false,
        category = "",
        visible = false,
        vehicleId = "",
        score = 0.0,
        status = ManualDeclarationSnapshotStatus.PROPOSED,
    )
}
