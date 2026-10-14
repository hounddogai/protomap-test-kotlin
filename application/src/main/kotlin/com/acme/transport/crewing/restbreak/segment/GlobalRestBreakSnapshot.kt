package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rest break snapshot is in its life cycle. */
enum class GlobalRestBreakSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global rest break snapshot. Its code is unique within a tenant. */
data class GlobalRestBreakSnapshot(
    var code: String,
    var title: String,
    var attempts: Int,
    var batchSize: Int,
    var endsAt: Int,
    var shiftCode: String,
    var category: String,
    var expiresAt: Int,
    var discount: Double,
    var status: GlobalRestBreakSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRestBreakSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRestBreakSnapshotStatus): Boolean {
        when (status) {
            GlobalRestBreakSnapshotStatus.IDLE -> {
                when (next) {
                    GlobalRestBreakSnapshotStatus.READY, GlobalRestBreakSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            GlobalRestBreakSnapshotStatus.WARMING -> {
                when (next) {
                    GlobalRestBreakSnapshotStatus.READY -> return true
                }
                return false
            }
            GlobalRestBreakSnapshotStatus.READY -> {
                when (next) {
                    GlobalRestBreakSnapshotStatus.DRAINING -> return true
                }
                return false
            }
            GlobalRestBreakSnapshotStatus.DRAINING -> return next == GlobalRestBreakSnapshotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalRestBreakSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("global rest break snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global rest break snapshot"} $code"

    fun effectiveLoad(): Double = round(discount * batchSize.toDouble() * 100) / 100

    fun snapshot(): GlobalRestBreakSnapshot {
        return GlobalRestBreakSnapshot(
            code = code,
            title = title,
            attempts = attempts,
            batchSize = batchSize,
            endsAt = endsAt,
            shiftCode = shiftCode,
            category = category,
            expiresAt = expiresAt,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a global rest break snapshot in status idle. */
fun newGlobalRestBreakSnapshot(code: String, title: String): GlobalRestBreakSnapshot {
    return GlobalRestBreakSnapshot(
        code = code,
        title = title,
        attempts = 0,
        batchSize = 0,
        endsAt = 0,
        shiftCode = "",
        category = "",
        expiresAt = 0,
        discount = 0.0,
        status = GlobalRestBreakSnapshotStatus.IDLE,
    )
}
