package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled hub group is in its life cycle. */
enum class ScheduledHubGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled hub group, with what the planners need to act on it. */
data class ScheduledHubGroup(
    var code: String,
    var label: String,
    var cost: Double,
    var vehicleId: String,
    var capacity: Int,
    var createdAt: Int,
    var approved: Boolean,
    var lane: String,
    var contactEmail: String,
    var sequence: Int,
    var maximum: Double,
    var status: ScheduledHubGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHubGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHubGroupStatus): Boolean {
        when (status) {
            ScheduledHubGroupStatus.DRAFT -> {
                when (next) {
                    ScheduledHubGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledHubGroupStatus.PENDING -> {
                when (next) {
                    ScheduledHubGroupStatus.SUSPENDED, ScheduledHubGroupStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledHubGroupStatus.ACTIVE -> {
                when (next) {
                    ScheduledHubGroupStatus.SUSPENDED, ScheduledHubGroupStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledHubGroupStatus.SUSPENDED -> {
                when (next) {
                    ScheduledHubGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHubGroupStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hub group $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"scheduled hub group"} $code"

    fun netTotal(): Double = round((cost - createdAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledHubGroup {
        return ScheduledHubGroup(
            code = code,
            label = label,
            cost = cost,
            vehicleId = vehicleId,
            capacity = capacity,
            createdAt = createdAt,
            approved = approved,
            lane = lane,
            contactEmail = contactEmail,
            sequence = sequence,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled hub group in status draft. */
fun newScheduledHubGroup(code: String, label: String): ScheduledHubGroup {
    return ScheduledHubGroup(
        code = code,
        label = label,
        cost = 0.0,
        vehicleId = "",
        capacity = 0,
        createdAt = 0,
        approved = false,
        lane = "",
        contactEmail = "",
        sequence = 0,
        maximum = 0.0,
        status = ScheduledHubGroupStatus.DRAFT,
    )
}
