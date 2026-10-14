package com.acme.transport.network.transittime.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time checkpoint is in its life cycle. */
enum class TransitTimeCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A transit time checkpoint as the transit time team keeps it. */
data class TransitTimeCheckpoint(
    var code: String,
    var name: String,
    var title: String,
    var channel: String,
    var externalRef: String,
    var score: Double,
    var etaMinutes: Int,
    var attempts: Int,
    var updatedAt: Int,
    var status: TransitTimeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeCheckpointStatus): Boolean {
        when (status) {
            TransitTimeCheckpointStatus.NEW -> {
                when (next) {
                    TransitTimeCheckpointStatus.DONE, TransitTimeCheckpointStatus.FAILED -> return true
                }
                return false
            }
            TransitTimeCheckpointStatus.QUEUED -> {
                when (next) {
                    TransitTimeCheckpointStatus.RUNNING, TransitTimeCheckpointStatus.FAILED -> return true
                }
                return false
            }
            TransitTimeCheckpointStatus.RUNNING -> return next == TransitTimeCheckpointStatus.FAILED
            TransitTimeCheckpointStatus.DONE -> {
                when (next) {
                    TransitTimeCheckpointStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("transit time checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((updatedAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeCheckpoint {
        return TransitTimeCheckpoint(
            code = code,
            name = name,
            title = title,
            channel = channel,
            externalRef = externalRef,
            score = score,
            etaMinutes = etaMinutes,
            attempts = attempts,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a transit time checkpoint in status new. */
fun newTransitTimeCheckpoint(code: String, name: String): TransitTimeCheckpoint {
    return TransitTimeCheckpoint(
        code = code,
        name = name,
        title = "",
        channel = "",
        externalRef = "",
        score = 0.0,
        etaMinutes = 0,
        attempts = 0,
        updatedAt = 0,
        status = TransitTimeCheckpointStatus.NEW,
    )
}
