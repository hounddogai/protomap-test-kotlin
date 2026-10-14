package com.acme.transport.safety.coaching.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled coaching snapshot is in its life cycle. */
enum class ScheduledCoachingSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled coaching snapshot. Its code is unique within a tenant. */
data class ScheduledCoachingSnapshot(
    var code: String,
    var name: String,
    var vehicleId: String,
    var discount: Double,
    var region: String,
    var etaMinutes: Int,
    var budget: Double,
    var lane: String,
    var rate: Double,
    var batchSize: Int,
    var description: String,
    var status: ScheduledCoachingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCoachingSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCoachingSnapshotStatus): Boolean {
        when (status) {
            ScheduledCoachingSnapshotStatus.NEW -> {
                when (next) {
                    ScheduledCoachingSnapshotStatus.QUEUED, ScheduledCoachingSnapshotStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledCoachingSnapshotStatus.QUEUED -> return next == ScheduledCoachingSnapshotStatus.RUNNING
            ScheduledCoachingSnapshotStatus.RUNNING -> return next == ScheduledCoachingSnapshotStatus.FAILED
            ScheduledCoachingSnapshotStatus.DONE -> {
                when (next) {
                    ScheduledCoachingSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCoachingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled coaching snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled coaching snapshot"} $code"

    fun weightedValue(): Double = round((rate + batchSize.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCoachingSnapshot {
        return ScheduledCoachingSnapshot(
            code = code,
            name = name,
            vehicleId = vehicleId,
            discount = discount,
            region = region,
            etaMinutes = etaMinutes,
            budget = budget,
            lane = lane,
            rate = rate,
            batchSize = batchSize,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled coaching snapshot in status new. */
fun newScheduledCoachingSnapshot(code: String, name: String): ScheduledCoachingSnapshot {
    return ScheduledCoachingSnapshot(
        code = code,
        name = name,
        vehicleId = "",
        discount = 0.0,
        region = "",
        etaMinutes = 0,
        budget = 0.0,
        lane = "",
        rate = 0.0,
        batchSize = 0,
        description = "",
        status = ScheduledCoachingSnapshotStatus.NEW,
    )
}
