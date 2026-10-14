package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss schedule is in its life cycle. */
enum class NearMissScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A near miss schedule. Its code is unique within a tenant. */
data class NearMissSchedule(
    var code: String,
    var name: String,
    var approved: Boolean,
    var etaMinutes: Int,
    var timezone: String,
    var windowDays: Int,
    var amount: Double,
    var weight: Double,
    var carrier: String,
    var region: String,
    var status: NearMissScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissScheduleStatus): Boolean {
        when (status) {
            NearMissScheduleStatus.NEW -> {
                when (next) {
                    NearMissScheduleStatus.RUNNING -> return true
                }
                return false
            }
            NearMissScheduleStatus.QUEUED -> {
                when (next) {
                    NearMissScheduleStatus.RUNNING, NearMissScheduleStatus.DONE -> return true
                }
                return false
            }
            NearMissScheduleStatus.RUNNING -> return next == NearMissScheduleStatus.DONE
            NearMissScheduleStatus.DONE -> {
                when (next) {
                    NearMissScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissScheduleStatus) {
        if (!canMoveTo(next)) {
            error("near miss schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round(amount * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): NearMissSchedule {
        return NearMissSchedule(
            code = code,
            name = name,
            approved = approved,
            etaMinutes = etaMinutes,
            timezone = timezone,
            windowDays = windowDays,
            amount = amount,
            weight = weight,
            carrier = carrier,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a near miss schedule in status new. */
fun newNearMissSchedule(code: String, name: String): NearMissSchedule {
    return NearMissSchedule(
        code = code,
        name = name,
        approved = false,
        etaMinutes = 0,
        timezone = "",
        windowDays = 0,
        amount = 0.0,
        weight = 0.0,
        carrier = "",
        region = "",
        status = NearMissScheduleStatus.NEW,
    )
}
