package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global redelivery snapshot is in its life cycle. */
enum class GlobalRedeliverySnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global redelivery snapshot. Its code is unique within a tenant. */
data class GlobalRedeliverySnapshot(
    var code: String,
    var name: String,
    var active: Boolean,
    var createdAt: Int,
    var region: String,
    var expiresAt: Int,
    var category: String,
    var priority: Int,
    var status: GlobalRedeliverySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRedeliverySnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRedeliverySnapshotStatus): Boolean {
        when (status) {
            GlobalRedeliverySnapshotStatus.DRAFT -> {
                when (next) {
                    GlobalRedeliverySnapshotStatus.SUSPENDED, GlobalRedeliverySnapshotStatus.CLOSED -> return true
                }
                return false
            }
            GlobalRedeliverySnapshotStatus.PENDING -> {
                when (next) {
                    GlobalRedeliverySnapshotStatus.CLOSED -> return true
                }
                return false
            }
            GlobalRedeliverySnapshotStatus.ACTIVE -> return next == GlobalRedeliverySnapshotStatus.CLOSED
            GlobalRedeliverySnapshotStatus.SUSPENDED -> {
                when (next) {
                    GlobalRedeliverySnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRedeliverySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("global redelivery snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global redelivery snapshot"} $code"

    fun weightedValue(): Double = round(createdAt.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): GlobalRedeliverySnapshot {
        return GlobalRedeliverySnapshot(
            code = code,
            name = name,
            active = active,
            createdAt = createdAt,
            region = region,
            expiresAt = expiresAt,
            category = category,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a global redelivery snapshot in status draft. */
fun newGlobalRedeliverySnapshot(code: String, name: String): GlobalRedeliverySnapshot {
    return GlobalRedeliverySnapshot(
        code = code,
        name = name,
        active = false,
        createdAt = 0,
        region = "",
        expiresAt = 0,
        category = "",
        priority = 0,
        status = GlobalRedeliverySnapshotStatus.DRAFT,
    )
}
