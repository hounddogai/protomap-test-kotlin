package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer group is in its life cycle. */
enum class TrailerGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a trailer group and its current status. */
data class TrailerGroup(
    var code: String,
    var label: String,
    var endsAt: Int,
    var minimum: Double,
    var startsAt: Int,
    var name: String,
    var revision: Int,
    var stopCount: Int,
    var expiresAt: Int,
    var status: TrailerGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerGroupStatus): Boolean {
        when (status) {
            TrailerGroupStatus.PLANNED -> return next == TrailerGroupStatus.SCHEDULED
            TrailerGroupStatus.SCHEDULED -> return next == TrailerGroupStatus.CANCELLED
            TrailerGroupStatus.IN_PROGRESS -> {
                when (next) {
                    TrailerGroupStatus.COMPLETED -> return true
                }
                return false
            }
            TrailerGroupStatus.COMPLETED -> return next == TrailerGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TrailerGroupStatus) {
        if (!canMoveTo(next)) {
            error("trailer group $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round((revision.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): TrailerGroup {
        return TrailerGroup(
            code = code,
            label = label,
            endsAt = endsAt,
            minimum = minimum,
            startsAt = startsAt,
            name = name,
            revision = revision,
            stopCount = stopCount,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer group in status planned. */
fun newTrailerGroup(code: String, label: String): TrailerGroup {
    return TrailerGroup(
        code = code,
        label = label,
        endsAt = 0,
        minimum = 0.0,
        startsAt = 0,
        name = "",
        revision = 0,
        stopCount = 0,
        expiresAt = 0,
        status = TrailerGroupStatus.PLANNED,
    )
}
