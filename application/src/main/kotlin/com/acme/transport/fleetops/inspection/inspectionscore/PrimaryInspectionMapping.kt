package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary inspection mapping is in its life cycle. */
enum class PrimaryInspectionMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One primary inspection mapping, with what the planners need to act on it. */
data class PrimaryInspectionMapping(
    var code: String,
    var label: String,
    var archived: Boolean,
    var graceDays: Int,
    var segment: String,
    var endsAt: Int,
    var score: Double,
    var weight: Double,
    var externalRef: String,
    var source: String,
    var distanceKm: Double,
    var status: PrimaryInspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryInspectionMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryInspectionMappingStatus): Boolean {
        when (status) {
            PrimaryInspectionMappingStatus.NEW -> return next == PrimaryInspectionMappingStatus.FAILED
            PrimaryInspectionMappingStatus.QUEUED -> return next == PrimaryInspectionMappingStatus.RUNNING
            PrimaryInspectionMappingStatus.RUNNING -> {
                when (next) {
                    PrimaryInspectionMappingStatus.DONE -> return true
                }
                return false
            }
            PrimaryInspectionMappingStatus.DONE -> return next == PrimaryInspectionMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: PrimaryInspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("primary inspection mapping $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedVolume(): Double = round(score * distanceKm * 100) / 100

    fun snapshot(): PrimaryInspectionMapping {
        return PrimaryInspectionMapping(
            code = code,
            label = label,
            archived = archived,
            graceDays = graceDays,
            segment = segment,
            endsAt = endsAt,
            score = score,
            weight = weight,
            externalRef = externalRef,
            source = source,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary inspection mapping in status new. */
fun newPrimaryInspectionMapping(code: String, label: String): PrimaryInspectionMapping {
    return PrimaryInspectionMapping(
        code = code,
        label = label,
        archived = false,
        graceDays = 0,
        segment = "",
        endsAt = 0,
        score = 0.0,
        weight = 0.0,
        externalRef = "",
        source = "",
        distanceKm = 0.0,
        status = PrimaryInspectionMappingStatus.NEW,
    )
}
