package com.acme.transport.emissions.idling.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The idling adjustment records of emissions's idling work, and the rules, stores, and calculations around
// them.

/** Where a idling adjustment is in its life cycle. */
enum class IdlingAdjustmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a idling adjustment and its current status. */
data class IdlingAdjustment(
    var code: String,
    var label: String,
    var visible: Boolean,
    var durationMinutes: Int,
    var locale: String,
    var timezone: String,
    var limit: Int,
    var routeCode: String,
    var status: IdlingAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingAdjustmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingAdjustmentStatus): Boolean {
        when (status) {
            IdlingAdjustmentStatus.REQUESTED -> {
                when (next) {
                    IdlingAdjustmentStatus.REVOKED, IdlingAdjustmentStatus.EXPIRED -> return true
                }
                return false
            }
            IdlingAdjustmentStatus.GRANTED -> {
                when (next) {
                    IdlingAdjustmentStatus.REVOKED -> return true
                }
                return false
            }
            IdlingAdjustmentStatus.REVOKED -> return next == IdlingAdjustmentStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: IdlingAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("idling adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedLoad(): Double = round((limit.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): IdlingAdjustment {
        return IdlingAdjustment(
            code = code,
            label = label,
            visible = visible,
            durationMinutes = durationMinutes,
            locale = locale,
            timezone = timezone,
            limit = limit,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a idling adjustment in status requested. */
fun newIdlingAdjustment(code: String, label: String): IdlingAdjustment {
    return IdlingAdjustment(
        code = code,
        label = label,
        visible = false,
        durationMinutes = 0,
        locale = "",
        timezone = "",
        limit = 0,
        routeCode = "",
        status = IdlingAdjustmentStatus.REQUESTED,
    )
}
