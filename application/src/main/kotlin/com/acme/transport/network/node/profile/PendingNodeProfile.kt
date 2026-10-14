package com.acme.transport.network.node.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending node profile records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a pending node profile is in its life cycle. */
enum class PendingNodeProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One pending node profile, with what the planners need to act on it. */
data class PendingNodeProfile(
    var code: String,
    var title: String,
    var startsAt: Int,
    var region: String,
    var locale: String,
    var fuelLiters: Double,
    var ratio: Double,
    var country: String,
    var limit: Int,
    var shiftCode: String,
    var status: PendingNodeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingNodeProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingNodeProfileStatus): Boolean {
        when (status) {
            PendingNodeProfileStatus.CREATED -> return next == PendingNodeProfileStatus.PUBLISHED || next == PendingNodeProfileStatus.RETIRED
            PendingNodeProfileStatus.VALIDATED -> return next == PendingNodeProfileStatus.PUBLISHED
            PendingNodeProfileStatus.PUBLISHED -> {
                when (next) {
                    PendingNodeProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingNodeProfileStatus) {
        if (!canMoveTo(next)) {
            error("pending node profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(fuelLiters * ratio * 100) / 100

    fun snapshot(): PendingNodeProfile {
        return PendingNodeProfile(
            code = code,
            title = title,
            startsAt = startsAt,
            region = region,
            locale = locale,
            fuelLiters = fuelLiters,
            ratio = ratio,
            country = country,
            limit = limit,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a pending node profile in status created. */
fun newPendingNodeProfile(code: String, title: String): PendingNodeProfile {
    return PendingNodeProfile(
        code = code,
        title = title,
        startsAt = 0,
        region = "",
        locale = "",
        fuelLiters = 0.0,
        ratio = 0.0,
        country = "",
        limit = 0,
        shiftCode = "",
        status = PendingNodeProfileStatus.CREATED,
    )
}
