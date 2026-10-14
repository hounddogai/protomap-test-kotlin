package com.acme.transport.terminal.appointment.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment rule is in its life cycle. */
enum class AppointmentRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a appointment rule and its current status. */
data class AppointmentRule(
    var code: String,
    var name: String,
    var weight: Double,
    var attempts: Int,
    var graceDays: Int,
    var driverId: String,
    var windowDays: Int,
    var margin: Double,
    var endsAt: Int,
    var status: AppointmentRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentRuleStatus): Boolean {
        when (status) {
            AppointmentRuleStatus.NEW -> return next == AppointmentRuleStatus.RUNNING || next == AppointmentRuleStatus.FAILED
            AppointmentRuleStatus.QUEUED -> {
                when (next) {
                    AppointmentRuleStatus.RUNNING, AppointmentRuleStatus.DONE -> return true
                }
                return false
            }
            AppointmentRuleStatus.RUNNING -> return next == AppointmentRuleStatus.DONE || next == AppointmentRuleStatus.FAILED
            AppointmentRuleStatus.DONE -> {
                when (next) {
                    AppointmentRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentRuleStatus) {
        if (!canMoveTo(next)) {
            error("appointment rule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedExposure(): Double = round((endsAt.toDouble() + weight) * 100) / 100

    fun snapshot(): AppointmentRule {
        return AppointmentRule(
            code = code,
            name = name,
            weight = weight,
            attempts = attempts,
            graceDays = graceDays,
            driverId = driverId,
            windowDays = windowDays,
            margin = margin,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment rule in status new. */
fun newAppointmentRule(code: String, name: String): AppointmentRule {
    return AppointmentRule(
        code = code,
        name = name,
        weight = 0.0,
        attempts = 0,
        graceDays = 0,
        driverId = "",
        windowDays = 0,
        margin = 0.0,
        endsAt = 0,
        status = AppointmentRuleStatus.NEW,
    )
}
