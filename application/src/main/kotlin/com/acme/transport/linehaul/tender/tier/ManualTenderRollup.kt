package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tender rollup is in its life cycle. */
enum class ManualTenderRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual tender rollup, with what the planners need to act on it. */
data class ManualTenderRollup(
    var code: String,
    var title: String,
    var revision: Int,
    var capacity: Int,
    var lane: String,
    var status: ManualTenderRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTenderRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTenderRollupStatus): Boolean {
        when (status) {
            ManualTenderRollupStatus.CREATED -> return next == ManualTenderRollupStatus.VALIDATED
            ManualTenderRollupStatus.VALIDATED -> return next == ManualTenderRollupStatus.PUBLISHED || next == ManualTenderRollupStatus.RETIRED
            ManualTenderRollupStatus.PUBLISHED -> return next == ManualTenderRollupStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualTenderRollupStatus) {
        if (!canMoveTo(next)) {
            error("manual tender rollup $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun projectedValue(): Double = round((revision.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualTenderRollup {
        return ManualTenderRollup(
            code = code,
            title = title,
            revision = revision,
            capacity = capacity,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual tender rollup in status created. */
fun newManualTenderRollup(code: String, title: String): ManualTenderRollup {
    return ManualTenderRollup(
        code = code,
        title = title,
        revision = 0,
        capacity = 0,
        lane = "",
        status = ManualTenderRollupStatus.CREATED,
    )
}
