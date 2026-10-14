package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global parcel profile records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a global parcel profile is in its life cycle. */
enum class GlobalParcelProfileStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global parcel profile, with what the planners need to act on it. */
data class GlobalParcelProfile(
    var code: String,
    var label: String,
    var sequence: Int,
    var tags: MutableList<String>,
    var createdAt: Int,
    var name: String,
    var capacity: Int,
    var fuelLiters: Double,
    var minimum: Double,
    var title: String,
    var budget: Double,
    var status: GlobalParcelProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalParcelProfileStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalParcelProfileStatus): Boolean {
        when (status) {
            GlobalParcelProfileStatus.PROPOSED -> {
                when (next) {
                    GlobalParcelProfileStatus.APPROVED, GlobalParcelProfileStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalParcelProfileStatus.APPROVED -> return next == GlobalParcelProfileStatus.REJECTED
            GlobalParcelProfileStatus.REJECTED -> return next == GlobalParcelProfileStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalParcelProfileStatus) {
        if (!canMoveTo(next)) {
            error("global parcel profile $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedTotal(): Double = round((sequence.toDouble() - budget) * 100) / 100

    fun snapshot(): GlobalParcelProfile {
        return GlobalParcelProfile(
            code = code,
            label = label,
            sequence = sequence,
            tags = tags,
            createdAt = createdAt,
            name = name,
            capacity = capacity,
            fuelLiters = fuelLiters,
            minimum = minimum,
            title = title,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global parcel profile in status proposed. */
fun newGlobalParcelProfile(code: String, label: String): GlobalParcelProfile {
    return GlobalParcelProfile(
        code = code,
        label = label,
        sequence = 0,
        tags = mutableListOf<String>(),
        createdAt = 0,
        name = "",
        capacity = 0,
        fuelLiters = 0.0,
        minimum = 0.0,
        title = "",
        budget = 0.0,
        status = GlobalParcelProfileStatus.PROPOSED,
    )
}
