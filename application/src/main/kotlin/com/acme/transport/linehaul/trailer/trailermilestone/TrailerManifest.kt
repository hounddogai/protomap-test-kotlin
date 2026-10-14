package com.acme.transport.linehaul.trailer.trailermilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer manifest is in its life cycle. */
enum class TrailerManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A trailer manifest. Its code is unique within a tenant. */
data class TrailerManifest(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var ceiling: Double,
    var archived: Boolean,
    var fuelLiters: Double,
    var revision: Int,
    var description: String,
    var summary: String,
    var status: TrailerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerManifestStatus): Boolean {
        when (status) {
            TrailerManifestStatus.NEW -> return next == TrailerManifestStatus.QUEUED || next == TrailerManifestStatus.RUNNING
            TrailerManifestStatus.QUEUED -> return next == TrailerManifestStatus.DONE || next == TrailerManifestStatus.FAILED
            TrailerManifestStatus.RUNNING -> return next == TrailerManifestStatus.FAILED
            TrailerManifestStatus.DONE -> return next == TrailerManifestStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TrailerManifestStatus) {
        if (!canMoveTo(next)) {
            error("trailer manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round((revision.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): TrailerManifest {
        return TrailerManifest(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            ceiling = ceiling,
            archived = archived,
            fuelLiters = fuelLiters,
            revision = revision,
            description = description,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a trailer manifest in status new. */
fun newTrailerManifest(code: String, title: String): TrailerManifest {
    return TrailerManifest(
        code = code,
        title = title,
        etaMinutes = 0,
        ceiling = 0.0,
        archived = false,
        fuelLiters = 0.0,
        revision = 0,
        description = "",
        summary = "",
        status = TrailerManifestStatus.NEW,
    )
}
