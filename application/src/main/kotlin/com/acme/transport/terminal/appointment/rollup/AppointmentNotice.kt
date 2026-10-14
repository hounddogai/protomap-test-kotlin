package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.normalize

/** Where a appointment notice is in its life cycle. */
enum class AppointmentNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One appointment notice, with what the planners need to act on it. */
data class AppointmentNotice(
    var code: String,
    var name: String,
    var locale: String,
    var depot: String,
    var phone: String,
    var quantity: Int,
    var status: AppointmentNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentNoticeStatus): Boolean {
        when (status) {
            AppointmentNoticeStatus.IDLE -> {
                when (next) {
                    AppointmentNoticeStatus.WARMING, AppointmentNoticeStatus.STOPPED -> return true
                }
                return false
            }
            AppointmentNoticeStatus.WARMING -> {
                when (next) {
                    AppointmentNoticeStatus.READY -> return true
                }
                return false
            }
            AppointmentNoticeStatus.READY -> {
                when (next) {
                    AppointmentNoticeStatus.STOPPED -> return true
                }
                return false
            }
            AppointmentNoticeStatus.DRAINING -> {
                when (next) {
                    AppointmentNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentNoticeStatus) {
        if (!canMoveTo(next)) {
            error("appointment notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"appointment notice"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): AppointmentNotice {
        return AppointmentNotice(
            code = code,
            name = name,
            locale = locale,
            depot = depot,
            phone = phone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a appointment notice in status idle. */
fun newAppointmentNotice(code: String, name: String): AppointmentNotice {
    return AppointmentNotice(
        code = code,
        name = name,
        locale = "",
        depot = "",
        phone = "",
        quantity = 0,
        status = AppointmentNoticeStatus.IDLE,
    )
}
