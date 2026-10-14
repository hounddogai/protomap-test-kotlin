package com.acme.transport.crewing.restbreak.breakprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break checkpoint is in its life cycle. */
enum class RestBreakCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rest break checkpoint and its current status. */
data class RestBreakCheckpoint(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var expiresAt: Int,
    var reviewer: String,
    var lane: String,
    var minimum: Double,
    var contactEmail: String,
    var quantity: Int,
    var carrier: String,
    var status: RestBreakCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakCheckpointStatus): Boolean {
        when (status) {
            RestBreakCheckpointStatus.IDLE -> {
                when (next) {
                    RestBreakCheckpointStatus.WARMING, RestBreakCheckpointStatus.DRAINING -> return true
                }
                return false
            }
            RestBreakCheckpointStatus.WARMING -> {
                when (next) {
                    RestBreakCheckpointStatus.READY, RestBreakCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakCheckpointStatus.READY -> return next == RestBreakCheckpointStatus.STOPPED
            RestBreakCheckpointStatus.DRAINING -> {
                when (next) {
                    RestBreakCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("rest break checkpoint $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round((minimum + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakCheckpoint {
        return RestBreakCheckpoint(
            code = code,
            title = title,
            attributes = attributes,
            expiresAt = expiresAt,
            reviewer = reviewer,
            lane = lane,
            minimum = minimum,
            contactEmail = contactEmail,
            quantity = quantity,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rest break checkpoint in status idle. */
fun newRestBreakCheckpoint(code: String, title: String): RestBreakCheckpoint {
    return RestBreakCheckpoint(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        expiresAt = 0,
        reviewer = "",
        lane = "",
        minimum = 0.0,
        contactEmail = "",
        quantity = 0,
        carrier = "",
        status = RestBreakCheckpointStatus.IDLE,
    )
}
