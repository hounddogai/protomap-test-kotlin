package com.acme.transport.network.transittime.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal transit time checkpoint is in its life cycle. */
enum class SeasonalTransitTimeCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal transit time checkpoint as the transit time team keeps it. */
data class SeasonalTransitTimeCheckpoint(
    var code: String,
    var title: String,
    var retries: Int,
    var version: Int,
    var contactEmail: String,
    var pinned: Boolean,
    var ratio: Double,
    var status: SeasonalTransitTimeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTransitTimeCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTransitTimeCheckpointStatus): Boolean {
        when (status) {
            SeasonalTransitTimeCheckpointStatus.DRAFT -> {
                when (next) {
                    SeasonalTransitTimeCheckpointStatus.PENDING, SeasonalTransitTimeCheckpointStatus.ACTIVE -> return true
                }
                return false
            }
            SeasonalTransitTimeCheckpointStatus.PENDING -> {
                when (next) {
                    SeasonalTransitTimeCheckpointStatus.ACTIVE, SeasonalTransitTimeCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalTransitTimeCheckpointStatus.ACTIVE -> {
                when (next) {
                    SeasonalTransitTimeCheckpointStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalTransitTimeCheckpointStatus.SUSPENDED -> {
                when (next) {
                    SeasonalTransitTimeCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTransitTimeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("seasonal transit time checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round((retries.toDouble() + ratio) * 100) / 100

    fun snapshot(): SeasonalTransitTimeCheckpoint {
        return SeasonalTransitTimeCheckpoint(
            code = code,
            title = title,
            retries = retries,
            version = version,
            contactEmail = contactEmail,
            pinned = pinned,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal transit time checkpoint in status draft. */
fun newSeasonalTransitTimeCheckpoint(code: String, title: String): SeasonalTransitTimeCheckpoint {
    return SeasonalTransitTimeCheckpoint(
        code = code,
        title = title,
        retries = 0,
        version = 0,
        contactEmail = "",
        pinned = false,
        ratio = 0.0,
        status = SeasonalTransitTimeCheckpointStatus.DRAFT,
    )
}
