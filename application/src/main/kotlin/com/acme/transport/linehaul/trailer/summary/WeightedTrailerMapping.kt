package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted trailer mapping is in its life cycle. */
enum class WeightedTrailerMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted trailer mapping, with what the planners need to act on it. */
data class WeightedTrailerMapping(
    var code: String,
    var title: String,
    var cost: Double,
    var amount: Double,
    var archived: Boolean,
    var score: Double,
    var lane: String,
    var status: WeightedTrailerMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerMappingStatus): Boolean {
        when (status) {
            WeightedTrailerMappingStatus.CREATED -> return next == WeightedTrailerMappingStatus.PUBLISHED || next == WeightedTrailerMappingStatus.RETIRED
            WeightedTrailerMappingStatus.VALIDATED -> return next == WeightedTrailerMappingStatus.PUBLISHED || next == WeightedTrailerMappingStatus.RETIRED
            WeightedTrailerMappingStatus.PUBLISHED -> return next == WeightedTrailerMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedTrailerMappingStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted trailer mapping"} $code"

    fun weightedValue(): Double = round(score * cost * 100) / 100

    fun snapshot(): WeightedTrailerMapping {
        return WeightedTrailerMapping(
            code = code,
            title = title,
            cost = cost,
            amount = amount,
            archived = archived,
            score = score,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted trailer mapping in status created. */
fun newWeightedTrailerMapping(code: String, title: String): WeightedTrailerMapping {
    return WeightedTrailerMapping(
        code = code,
        title = title,
        cost = 0.0,
        amount = 0.0,
        archived = false,
        score = 0.0,
        lane = "",
        status = WeightedTrailerMappingStatus.CREATED,
    )
}
