package com.acme.transport.crewing.roster.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient roster batch is in its life cycle. */
enum class LenientRosterBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One lenient roster batch, with what the planners need to act on it. */
data class LenientRosterBatch(
    var code: String,
    var label: String,
    var loadKg: Double,
    var lane: String,
    var margin: Double,
    var unit: String,
    var status: LenientRosterBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRosterBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRosterBatchStatus): Boolean {
        when (status) {
            LenientRosterBatchStatus.CREATED -> return next == LenientRosterBatchStatus.PUBLISHED || next == LenientRosterBatchStatus.RETIRED
            LenientRosterBatchStatus.VALIDATED -> return next == LenientRosterBatchStatus.RETIRED
            LenientRosterBatchStatus.PUBLISHED -> return next == LenientRosterBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: LenientRosterBatchStatus) {
        if (!canMoveTo(next)) {
            error("lenient roster batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((margin - loadKg) * 100) / 100

    fun snapshot(): LenientRosterBatch {
        return LenientRosterBatch(
            code = code,
            label = label,
            loadKg = loadKg,
            lane = lane,
            margin = margin,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lenient roster batch in status created. */
fun newLenientRosterBatch(code: String, label: String): LenientRosterBatch {
    return LenientRosterBatch(
        code = code,
        label = label,
        loadKg = 0.0,
        lane = "",
        margin = 0.0,
        unit = "",
        status = LenientRosterBatchStatus.CREATED,
    )
}
