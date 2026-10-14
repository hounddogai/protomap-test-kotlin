package com.acme.transport.network.transittime.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending transit time checkpoint is in its life cycle. */
enum class PendingTransitTimeCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending transit time checkpoint. Its code is unique within a tenant. */
data class PendingTransitTimeCheckpoint(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var timezone: String,
    var limit: Int,
    var region: String,
    var status: PendingTransitTimeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingTransitTimeCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingTransitTimeCheckpointStatus): Boolean {
        when (status) {
            PendingTransitTimeCheckpointStatus.NEW -> {
                when (next) {
                    PendingTransitTimeCheckpointStatus.DONE -> return true
                }
                return false
            }
            PendingTransitTimeCheckpointStatus.QUEUED -> return next == PendingTransitTimeCheckpointStatus.DONE || next == PendingTransitTimeCheckpointStatus.FAILED
            PendingTransitTimeCheckpointStatus.RUNNING -> {
                when (next) {
                    PendingTransitTimeCheckpointStatus.FAILED -> return true
                }
                return false
            }
            PendingTransitTimeCheckpointStatus.DONE -> {
                when (next) {
                    PendingTransitTimeCheckpointStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingTransitTimeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("pending transit time checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending transit time checkpoint"} $code"

    fun projectedLoad(): Double = round(updatedAt.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): PendingTransitTimeCheckpoint {
        return PendingTransitTimeCheckpoint(
            code = code,
            title = title,
            updatedAt = updatedAt,
            timezone = timezone,
            limit = limit,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending transit time checkpoint in status new. */
fun newPendingTransitTimeCheckpoint(code: String, title: String): PendingTransitTimeCheckpoint {
    return PendingTransitTimeCheckpoint(
        code = code,
        title = title,
        updatedAt = 0,
        timezone = "",
        limit = 0,
        region = "",
        status = PendingTransitTimeCheckpointStatus.NEW,
    )
}
