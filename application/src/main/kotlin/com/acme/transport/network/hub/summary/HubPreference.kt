package com.acme.transport.network.hub.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub preference is in its life cycle. */
enum class HubPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a hub preference and its current status. */
data class HubPreference(
    var code: String,
    var name: String,
    var floorValue: Double,
    var minimum: Double,
    var startsAt: Int,
    var attempts: Int,
    var visible: Boolean,
    var category: String,
    var sequence: Int,
    var threshold: Double,
    var ratio: Double,
    var status: HubPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubPreferenceStatus): Boolean {
        when (status) {
            HubPreferenceStatus.PROPOSED -> return next == HubPreferenceStatus.APPROVED || next == HubPreferenceStatus.WITHDRAWN
            HubPreferenceStatus.APPROVED -> {
                when (next) {
                    HubPreferenceStatus.REJECTED, HubPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
            HubPreferenceStatus.REJECTED -> {
                when (next) {
                    HubPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("hub preference $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedExposure(): Double = round((startsAt.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): HubPreference {
        return HubPreference(
            code = code,
            name = name,
            floorValue = floorValue,
            minimum = minimum,
            startsAt = startsAt,
            attempts = attempts,
            visible = visible,
            category = category,
            sequence = sequence,
            threshold = threshold,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a hub preference in status proposed. */
fun newHubPreference(code: String, name: String): HubPreference {
    return HubPreference(
        code = code,
        name = name,
        floorValue = 0.0,
        minimum = 0.0,
        startsAt = 0,
        attempts = 0,
        visible = false,
        category = "",
        sequence = 0,
        threshold = 0.0,
        ratio = 0.0,
        status = HubPreferenceStatus.PROPOSED,
    )
}
