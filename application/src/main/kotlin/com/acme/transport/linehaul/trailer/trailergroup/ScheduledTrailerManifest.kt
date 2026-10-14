package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled trailer manifest is in its life cycle. */
enum class ScheduledTrailerManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a scheduled trailer manifest and its current status. */
data class ScheduledTrailerManifest(
    var code: String,
    var title: String,
    var active: Boolean,
    var limit: Int,
    var checksum: String,
    var revision: Int,
    var floorValue: Double,
    var shiftCode: String,
    var status: ScheduledTrailerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerManifestStatus): Boolean {
        when (status) {
            ScheduledTrailerManifestStatus.PLANNED -> return next == ScheduledTrailerManifestStatus.IN_PROGRESS || next == ScheduledTrailerManifestStatus.COMPLETED
            ScheduledTrailerManifestStatus.SCHEDULED -> {
                when (next) {
                    ScheduledTrailerManifestStatus.IN_PROGRESS, ScheduledTrailerManifestStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledTrailerManifestStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledTrailerManifestStatus.COMPLETED -> return true
                }
                return false
            }
            ScheduledTrailerManifestStatus.COMPLETED -> {
                when (next) {
                    ScheduledTrailerManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerManifestStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round(limit.toDouble() * floorValue * 100) / 100

    fun snapshot(): ScheduledTrailerManifest {
        return ScheduledTrailerManifest(
            code = code,
            title = title,
            active = active,
            limit = limit,
            checksum = checksum,
            revision = revision,
            floorValue = floorValue,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled trailer manifest in status planned. */
fun newScheduledTrailerManifest(code: String, title: String): ScheduledTrailerManifest {
    return ScheduledTrailerManifest(
        code = code,
        title = title,
        active = false,
        limit = 0,
        checksum = "",
        revision = 0,
        floorValue = 0.0,
        shiftCode = "",
        status = ScheduledTrailerManifestStatus.PLANNED,
    )
}
