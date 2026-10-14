package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped lane plan preference is in its life cycle. */
enum class CappedLanePlanPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped lane plan preference as the lane plan team keeps it. */
data class CappedLanePlanPreference(
    var code: String,
    var title: String,
    var carrier: String,
    var score: Double,
    var lane: String,
    var reviewer: String,
    var ceiling: Double,
    var budget: Double,
    var weight: Double,
    var floorValue: Double,
    var phone: String,
    var status: CappedLanePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedLanePlanPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedLanePlanPreferenceStatus): Boolean {
        when (status) {
            CappedLanePlanPreferenceStatus.PLANNED -> return next == CappedLanePlanPreferenceStatus.IN_PROGRESS || next == CappedLanePlanPreferenceStatus.COMPLETED
            CappedLanePlanPreferenceStatus.SCHEDULED -> return next == CappedLanePlanPreferenceStatus.IN_PROGRESS || next == CappedLanePlanPreferenceStatus.CANCELLED
            CappedLanePlanPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    CappedLanePlanPreferenceStatus.COMPLETED, CappedLanePlanPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            CappedLanePlanPreferenceStatus.COMPLETED -> return next == CappedLanePlanPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedLanePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capped lane plan preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedExposure(): Double = round((weight + score) * 100) / 100

    fun snapshot(): CappedLanePlanPreference {
        return CappedLanePlanPreference(
            code = code,
            title = title,
            carrier = carrier,
            score = score,
            lane = lane,
            reviewer = reviewer,
            ceiling = ceiling,
            budget = budget,
            weight = weight,
            floorValue = floorValue,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a capped lane plan preference in status planned. */
fun newCappedLanePlanPreference(code: String, title: String): CappedLanePlanPreference {
    return CappedLanePlanPreference(
        code = code,
        title = title,
        carrier = "",
        score = 0.0,
        lane = "",
        reviewer = "",
        ceiling = 0.0,
        budget = 0.0,
        weight = 0.0,
        floorValue = 0.0,
        phone = "",
        status = CappedLanePlanPreferenceStatus.PLANNED,
    )
}
