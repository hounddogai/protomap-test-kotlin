package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default backhaul snapshot is in its life cycle. */
enum class DefaultBackhaulSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A default backhaul snapshot. Its code is unique within a tenant. */
data class DefaultBackhaulSnapshot(
    var code: String,
    var title: String,
    var currency: String,
    var createdAt: Int,
    var unit: String,
    var pallets: Int,
    var floorValue: Double,
    var limit: Int,
    var retries: Int,
    var expiresAt: Int,
    var status: DefaultBackhaulSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultBackhaulSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultBackhaulSnapshotStatus): Boolean {
        when (status) {
            DefaultBackhaulSnapshotStatus.PLANNED -> {
                when (next) {
                    DefaultBackhaulSnapshotStatus.COMPLETED, DefaultBackhaulSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            DefaultBackhaulSnapshotStatus.SCHEDULED -> {
                when (next) {
                    DefaultBackhaulSnapshotStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefaultBackhaulSnapshotStatus.IN_PROGRESS -> return next == DefaultBackhaulSnapshotStatus.COMPLETED || next == DefaultBackhaulSnapshotStatus.CANCELLED
            DefaultBackhaulSnapshotStatus.COMPLETED -> {
                when (next) {
                    DefaultBackhaulSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultBackhaulSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("default backhaul snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round(floorValue * pallets.toDouble() * 100) / 100

    fun snapshot(): DefaultBackhaulSnapshot {
        return DefaultBackhaulSnapshot(
            code = code,
            title = title,
            currency = currency,
            createdAt = createdAt,
            unit = unit,
            pallets = pallets,
            floorValue = floorValue,
            limit = limit,
            retries = retries,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a default backhaul snapshot in status planned. */
fun newDefaultBackhaulSnapshot(code: String, title: String): DefaultBackhaulSnapshot {
    return DefaultBackhaulSnapshot(
        code = code,
        title = title,
        currency = "",
        createdAt = 0,
        unit = "",
        pallets = 0,
        floorValue = 0.0,
        limit = 0,
        retries = 0,
        expiresAt = 0,
        status = DefaultBackhaulSnapshotStatus.PLANNED,
    )
}
