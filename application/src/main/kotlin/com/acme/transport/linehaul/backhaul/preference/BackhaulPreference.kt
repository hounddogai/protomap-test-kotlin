package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.text.normalize

// The backhaul preference records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul preference is in its life cycle. */
enum class BackhaulPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A backhaul preference as the backhaul team keeps it. */
data class BackhaulPreference(
    var code: String,
    var name: String,
    var reviewer: String,
    var active: Boolean,
    var archived: Boolean,
    var channel: String,
    var fuelLiters: Double,
    var ownerId: String,
    var status: BackhaulPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulPreferenceStatus): Boolean {
        when (status) {
            BackhaulPreferenceStatus.NEW -> {
                when (next) {
                    BackhaulPreferenceStatus.RUNNING, BackhaulPreferenceStatus.DONE -> return true
                }
                return false
            }
            BackhaulPreferenceStatus.QUEUED -> {
                when (next) {
                    BackhaulPreferenceStatus.DONE -> return true
                }
                return false
            }
            BackhaulPreferenceStatus.RUNNING -> return next == BackhaulPreferenceStatus.DONE
            BackhaulPreferenceStatus.DONE -> return next == BackhaulPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BackhaulPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("backhaul preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): BackhaulPreference {
        return BackhaulPreference(
            code = code,
            name = name,
            reviewer = reviewer,
            active = active,
            archived = archived,
            channel = channel,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a backhaul preference in status new. */
fun newBackhaulPreference(code: String, name: String): BackhaulPreference {
    return BackhaulPreference(
        code = code,
        name = name,
        reviewer = "",
        active = false,
        archived = false,
        channel = "",
        fuelLiters = 0.0,
        ownerId = "",
        status = BackhaulPreferenceStatus.NEW,
    )
}
