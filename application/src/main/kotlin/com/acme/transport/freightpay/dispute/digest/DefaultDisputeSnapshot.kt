package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default dispute snapshot is in its life cycle. */
enum class DefaultDisputeSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default dispute snapshot. Its code is unique within a tenant. */
data class DefaultDisputeSnapshot(
    var code: String,
    var label: String,
    var driverId: String,
    var windowDays: Int,
    var createdAt: Int,
    var description: String,
    var rate: Double,
    var name: String,
    var status: DefaultDisputeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDisputeSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDisputeSnapshotStatus): Boolean {
        when (status) {
            DefaultDisputeSnapshotStatus.CREATED -> {
                when (next) {
                    DefaultDisputeSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultDisputeSnapshotStatus.VALIDATED -> {
                when (next) {
                    DefaultDisputeSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            DefaultDisputeSnapshotStatus.PUBLISHED -> return next == DefaultDisputeSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultDisputeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default dispute snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((rate + createdAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultDisputeSnapshot {
        return DefaultDisputeSnapshot(
            code = code,
            label = label,
            driverId = driverId,
            windowDays = windowDays,
            createdAt = createdAt,
            description = description,
            rate = rate,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default dispute snapshot in status created. */
fun newDefaultDisputeSnapshot(code: String, label: String): DefaultDisputeSnapshot {
    return DefaultDisputeSnapshot(
        code = code,
        label = label,
        driverId = "",
        windowDays = 0,
        createdAt = 0,
        description = "",
        rate = 0.0,
        name = "",
        status = DefaultDisputeSnapshotStatus.CREATED,
    )
}
