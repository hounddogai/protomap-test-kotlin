package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending relay segment records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a pending relay segment is in its life cycle. */
enum class PendingRelaySegmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a pending relay segment and its current status. */
data class PendingRelaySegment(
    var code: String,
    var name: String,
    var createdAt: Int,
    var description: String,
    var fuelLiters: Double,
    var approved: Boolean,
    var status: PendingRelaySegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingRelaySegmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingRelaySegmentStatus): Boolean {
        when (status) {
            PendingRelaySegmentStatus.NEW -> return next == PendingRelaySegmentStatus.FAILED
            PendingRelaySegmentStatus.QUEUED -> return next == PendingRelaySegmentStatus.RUNNING || next == PendingRelaySegmentStatus.FAILED
            PendingRelaySegmentStatus.RUNNING -> return next == PendingRelaySegmentStatus.DONE || next == PendingRelaySegmentStatus.FAILED
            PendingRelaySegmentStatus.DONE -> {
                when (next) {
                    PendingRelaySegmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingRelaySegmentStatus) {
        if (!canMoveTo(next)) {
            error("pending relay segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"pending relay segment"} $code"

    fun adjustedLoad(): Double = round((fuelLiters + createdAt.toDouble()) * 100) / 100

    fun snapshot(): PendingRelaySegment {
        return PendingRelaySegment(
            code = code,
            name = name,
            createdAt = createdAt,
            description = description,
            fuelLiters = fuelLiters,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending relay segment in status new. */
fun newPendingRelaySegment(code: String, name: String): PendingRelaySegment {
    return PendingRelaySegment(
        code = code,
        name = name,
        createdAt = 0,
        description = "",
        fuelLiters = 0.0,
        approved = false,
        status = PendingRelaySegmentStatus.NEW,
    )
}
