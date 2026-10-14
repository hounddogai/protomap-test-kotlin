package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan preference is in its life cycle. */
enum class LanePlanPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lane plan preference and its current status. */
data class LanePlanPreference(
    var code: String,
    var label: String,
    var capacity: Int,
    var sequence: Int,
    var floorValue: Double,
    var ratio: Double,
    var dock: String,
    var externalRef: String,
    var title: String,
    var depot: String,
    var status: LanePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanPreferenceStatus): Boolean {
        when (status) {
            LanePlanPreferenceStatus.IDLE -> {
                when (next) {
                    LanePlanPreferenceStatus.DRAINING -> return true
                }
                return false
            }
            LanePlanPreferenceStatus.WARMING -> return next == LanePlanPreferenceStatus.READY || next == LanePlanPreferenceStatus.DRAINING
            LanePlanPreferenceStatus.READY -> return next == LanePlanPreferenceStatus.DRAINING || next == LanePlanPreferenceStatus.STOPPED
            LanePlanPreferenceStatus.DRAINING -> {
                when (next) {
                    LanePlanPreferenceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("lane plan preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedExposure(): Double = round((capacity.toDouble() + ratio) * 100) / 100

    fun snapshot(): LanePlanPreference {
        return LanePlanPreference(
            code = code,
            label = label,
            capacity = capacity,
            sequence = sequence,
            floorValue = floorValue,
            ratio = ratio,
            dock = dock,
            externalRef = externalRef,
            title = title,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lane plan preference in status idle. */
fun newLanePlanPreference(code: String, label: String): LanePlanPreference {
    return LanePlanPreference(
        code = code,
        label = label,
        capacity = 0,
        sequence = 0,
        floorValue = 0.0,
        ratio = 0.0,
        dock = "",
        externalRef = "",
        title = "",
        depot = "",
        status = LanePlanPreferenceStatus.IDLE,
    )
}
