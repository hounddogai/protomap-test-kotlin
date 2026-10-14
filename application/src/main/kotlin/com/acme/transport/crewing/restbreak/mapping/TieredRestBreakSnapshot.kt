package com.acme.transport.crewing.restbreak.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered rest break snapshot is in its life cycle. */
enum class TieredRestBreakSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tiered rest break snapshot. Its code is unique within a tenant. */
data class TieredRestBreakSnapshot(
    var code: String,
    var name: String,
    var lane: String,
    var phone: String,
    var floorValue: Double,
    var threshold: Double,
    var active: Boolean,
    var createdAt: Int,
    var externalRef: String,
    var status: TieredRestBreakSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRestBreakSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRestBreakSnapshotStatus): Boolean {
        when (status) {
            TieredRestBreakSnapshotStatus.REQUESTED -> return next == TieredRestBreakSnapshotStatus.GRANTED || next == TieredRestBreakSnapshotStatus.EXPIRED
            TieredRestBreakSnapshotStatus.GRANTED -> {
                when (next) {
                    TieredRestBreakSnapshotStatus.REVOKED, TieredRestBreakSnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            TieredRestBreakSnapshotStatus.REVOKED -> return next == TieredRestBreakSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredRestBreakSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("tiered rest break snapshot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((threshold + floorValue) * 100) / 100

    fun snapshot(): TieredRestBreakSnapshot {
        return TieredRestBreakSnapshot(
            code = code,
            name = name,
            lane = lane,
            phone = phone,
            floorValue = floorValue,
            threshold = threshold,
            active = active,
            createdAt = createdAt,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered rest break snapshot in status requested. */
fun newTieredRestBreakSnapshot(code: String, name: String): TieredRestBreakSnapshot {
    return TieredRestBreakSnapshot(
        code = code,
        name = name,
        lane = "",
        phone = "",
        floorValue = 0.0,
        threshold = 0.0,
        active = false,
        createdAt = 0,
        externalRef = "",
        status = TieredRestBreakSnapshotStatus.REQUESTED,
    )
}
