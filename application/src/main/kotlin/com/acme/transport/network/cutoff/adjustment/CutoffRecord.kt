package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff record is in its life cycle. */
enum class CutoffRecordStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One cutoff record, with what the planners need to act on it. */
data class CutoffRecord(
    var code: String,
    var label: String,
    var margin: Double,
    var pinned: Boolean,
    var distanceKm: Double,
    var threshold: Double,
    var limit: Int,
    var status: CutoffRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffRecordStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffRecordStatus): Boolean {
        when (status) {
            CutoffRecordStatus.CREATED -> return next == CutoffRecordStatus.PUBLISHED || next == CutoffRecordStatus.RETIRED
            CutoffRecordStatus.VALIDATED -> {
                when (next) {
                    CutoffRecordStatus.PUBLISHED -> return true
                }
                return false
            }
            CutoffRecordStatus.PUBLISHED -> return next == CutoffRecordStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CutoffRecordStatus) {
        if (!canMoveTo(next)) {
            error("cutoff record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedVolume(): Double = round((threshold - margin) * 100) / 100

    fun snapshot(): CutoffRecord {
        return CutoffRecord(
            code = code,
            label = label,
            margin = margin,
            pinned = pinned,
            distanceKm = distanceKm,
            threshold = threshold,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a cutoff record in status created. */
fun newCutoffRecord(code: String, label: String): CutoffRecord {
    return CutoffRecord(
        code = code,
        label = label,
        margin = 0.0,
        pinned = false,
        distanceKm = 0.0,
        threshold = 0.0,
        limit = 0,
        status = CutoffRecordStatus.CREATED,
    )
}
