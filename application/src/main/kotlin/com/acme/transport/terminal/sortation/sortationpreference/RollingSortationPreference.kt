package com.acme.transport.terminal.sortation.sortationpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling sortation preference records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a rolling sortation preference is in its life cycle. */
enum class RollingSortationPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling sortation preference. Its code is unique within a tenant. */
data class RollingSortationPreference(
    var code: String,
    var title: String,
    var odometer: Int,
    var channel: String,
    var lane: String,
    var name: String,
    var region: String,
    var capacity: Int,
    var status: RollingSortationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingSortationPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingSortationPreferenceStatus): Boolean {
        when (status) {
            RollingSortationPreferenceStatus.PROPOSED -> return next == RollingSortationPreferenceStatus.APPROVED || next == RollingSortationPreferenceStatus.REJECTED
            RollingSortationPreferenceStatus.APPROVED -> return next == RollingSortationPreferenceStatus.WITHDRAWN
            RollingSortationPreferenceStatus.REJECTED -> return next == RollingSortationPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingSortationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rolling sortation preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedVolume(): Double = round((capacity.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): RollingSortationPreference {
        return RollingSortationPreference(
            code = code,
            title = title,
            odometer = odometer,
            channel = channel,
            lane = lane,
            name = name,
            region = region,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rolling sortation preference in status proposed. */
fun newRollingSortationPreference(code: String, title: String): RollingSortationPreference {
    return RollingSortationPreference(
        code = code,
        title = title,
        odometer = 0,
        channel = "",
        lane = "",
        name = "",
        region = "",
        capacity = 0,
        status = RollingSortationPreferenceStatus.PROPOSED,
    )
}
