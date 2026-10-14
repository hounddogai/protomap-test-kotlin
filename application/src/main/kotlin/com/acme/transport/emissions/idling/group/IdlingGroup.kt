package com.acme.transport.emissions.idling.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling group records of emissions's idling work, and the rules, stores, and calculations around them.

/** Where a idling group is in its life cycle. */
enum class IdlingGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a idling group and its current status. */
data class IdlingGroup(
    var code: String,
    var title: String,
    var budget: Double,
    var carrier: String,
    var expiresAt: Int,
    var driverId: String,
    var margin: Double,
    var notes: String,
    var category: String,
    var status: IdlingGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingGroupStatus): Boolean {
        when (status) {
            IdlingGroupStatus.REQUESTED -> {
                when (next) {
                    IdlingGroupStatus.EXPIRED -> return true
                }
                return false
            }
            IdlingGroupStatus.GRANTED -> {
                when (next) {
                    IdlingGroupStatus.EXPIRED -> return true
                }
                return false
            }
            IdlingGroupStatus.REVOKED -> return next == IdlingGroupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: IdlingGroupStatus) {
        if (!canMoveTo(next)) {
            error("idling group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"idling group"} $code"

    fun projectedTotal(): Double = round(margin * expiresAt.toDouble() * 100) / 100

    fun snapshot(): IdlingGroup {
        return IdlingGroup(
            code = code,
            title = title,
            budget = budget,
            carrier = carrier,
            expiresAt = expiresAt,
            driverId = driverId,
            margin = margin,
            notes = notes,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a idling group in status requested. */
fun newIdlingGroup(code: String, title: String): IdlingGroup {
    return IdlingGroup(
        code = code,
        title = title,
        budget = 0.0,
        carrier = "",
        expiresAt = 0,
        driverId = "",
        margin = 0.0,
        notes = "",
        category = "",
        status = IdlingGroupStatus.REQUESTED,
    )
}
