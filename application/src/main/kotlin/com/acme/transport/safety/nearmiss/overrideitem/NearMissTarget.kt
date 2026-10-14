package com.acme.transport.safety.nearmiss.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss target is in its life cycle. */
enum class NearMissTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a near miss target and its current status. */
data class NearMissTarget(
    var code: String,
    var label: String,
    var lane: String,
    var approved: Boolean,
    var country: String,
    var windowDays: Int,
    var region: String,
    var attempts: Int,
    var maximum: Double,
    var status: NearMissTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissTargetStatus): Boolean {
        when (status) {
            NearMissTargetStatus.CREATED -> return next == NearMissTargetStatus.VALIDATED
            NearMissTargetStatus.VALIDATED -> return next == NearMissTargetStatus.PUBLISHED || next == NearMissTargetStatus.RETIRED
            NearMissTargetStatus.PUBLISHED -> {
                when (next) {
                    NearMissTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissTargetStatus) {
        if (!canMoveTo(next)) {
            error("near miss target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round((attempts.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): NearMissTarget {
        return NearMissTarget(
            code = code,
            label = label,
            lane = lane,
            approved = approved,
            country = country,
            windowDays = windowDays,
            region = region,
            attempts = attempts,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a near miss target in status created. */
fun newNearMissTarget(code: String, label: String): NearMissTarget {
    return NearMissTarget(
        code = code,
        label = label,
        lane = "",
        approved = false,
        country = "",
        windowDays = 0,
        region = "",
        attempts = 0,
        maximum = 0.0,
        status = NearMissTargetStatus.CREATED,
    )
}
