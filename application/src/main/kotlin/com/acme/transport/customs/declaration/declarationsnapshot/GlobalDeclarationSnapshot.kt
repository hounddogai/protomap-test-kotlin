package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global declaration snapshot records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a global declaration snapshot is in its life cycle. */
enum class GlobalDeclarationSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global declaration snapshot as the declaration team keeps it. */
data class GlobalDeclarationSnapshot(
    var code: String,
    var label: String,
    var retries: Int,
    var checksum: String,
    var createdAt: Int,
    var archived: Boolean,
    var graceDays: Int,
    var stopCount: Int,
    var status: GlobalDeclarationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalDeclarationSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalDeclarationSnapshotStatus): Boolean {
        when (status) {
            GlobalDeclarationSnapshotStatus.NEW -> {
                when (next) {
                    GlobalDeclarationSnapshotStatus.RUNNING -> return true
                }
                return false
            }
            GlobalDeclarationSnapshotStatus.QUEUED -> {
                when (next) {
                    GlobalDeclarationSnapshotStatus.RUNNING, GlobalDeclarationSnapshotStatus.DONE -> return true
                }
                return false
            }
            GlobalDeclarationSnapshotStatus.RUNNING -> {
                when (next) {
                    GlobalDeclarationSnapshotStatus.DONE, GlobalDeclarationSnapshotStatus.FAILED -> return true
                }
                return false
            }
            GlobalDeclarationSnapshotStatus.DONE -> return next == GlobalDeclarationSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalDeclarationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("global declaration snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedExposure(): Double = round(createdAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): GlobalDeclarationSnapshot {
        return GlobalDeclarationSnapshot(
            code = code,
            label = label,
            retries = retries,
            checksum = checksum,
            createdAt = createdAt,
            archived = archived,
            graceDays = graceDays,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global declaration snapshot in status new. */
fun newGlobalDeclarationSnapshot(code: String, label: String): GlobalDeclarationSnapshot {
    return GlobalDeclarationSnapshot(
        code = code,
        label = label,
        retries = 0,
        checksum = "",
        createdAt = 0,
        archived = false,
        graceDays = 0,
        stopCount = 0,
        status = GlobalDeclarationSnapshotStatus.NEW,
    )
}
