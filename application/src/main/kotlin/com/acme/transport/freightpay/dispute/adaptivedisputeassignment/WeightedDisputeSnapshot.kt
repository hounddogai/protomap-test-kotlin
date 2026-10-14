package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted dispute snapshot is in its life cycle. */
enum class WeightedDisputeSnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted dispute snapshot. Its code is unique within a tenant. */
data class WeightedDisputeSnapshot(
    var code: String,
    var label: String,
    var summary: String,
    var externalRef: String,
    var checksum: String,
    var dock: String,
    var driverId: String,
    var durationMinutes: Int,
    var windowDays: Int,
    var batchSize: Int,
    var channel: String,
    var status: WeightedDisputeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDisputeSnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDisputeSnapshotStatus): Boolean {
        when (status) {
            WeightedDisputeSnapshotStatus.REQUESTED -> return next == WeightedDisputeSnapshotStatus.GRANTED || next == WeightedDisputeSnapshotStatus.REVOKED
            WeightedDisputeSnapshotStatus.GRANTED -> {
                when (next) {
                    WeightedDisputeSnapshotStatus.REVOKED -> return true
                }
                return false
            }
            WeightedDisputeSnapshotStatus.REVOKED -> return next == WeightedDisputeSnapshotStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: WeightedDisputeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("weighted dispute snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round(batchSize.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): WeightedDisputeSnapshot {
        return WeightedDisputeSnapshot(
            code = code,
            label = label,
            summary = summary,
            externalRef = externalRef,
            checksum = checksum,
            dock = dock,
            driverId = driverId,
            durationMinutes = durationMinutes,
            windowDays = windowDays,
            batchSize = batchSize,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted dispute snapshot in status requested. */
fun newWeightedDisputeSnapshot(code: String, label: String): WeightedDisputeSnapshot {
    return WeightedDisputeSnapshot(
        code = code,
        label = label,
        summary = "",
        externalRef = "",
        checksum = "",
        dock = "",
        driverId = "",
        durationMinutes = 0,
        windowDays = 0,
        batchSize = 0,
        channel = "",
        status = WeightedDisputeSnapshotStatus.REQUESTED,
    )
}
