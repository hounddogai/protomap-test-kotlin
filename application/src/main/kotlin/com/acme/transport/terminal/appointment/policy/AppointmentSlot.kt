package com.acme.transport.terminal.appointment.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment slot is in its life cycle. */
enum class AppointmentSlotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One appointment slot, with what the planners need to act on it. */
data class AppointmentSlot(
    var code: String,
    var title: String,
    var limit: Int,
    var shiftCode: String,
    var batchSize: Int,
    var updatedAt: Int,
    var visible: Boolean,
    var currency: String,
    var createdAt: Int,
    var etaMinutes: Int,
    var checksum: String,
    var status: AppointmentSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentSlotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentSlotStatus): Boolean {
        when (status) {
            AppointmentSlotStatus.PLANNED -> return next == AppointmentSlotStatus.SCHEDULED
            AppointmentSlotStatus.SCHEDULED -> {
                when (next) {
                    AppointmentSlotStatus.COMPLETED, AppointmentSlotStatus.CANCELLED -> return true
                }
                return false
            }
            AppointmentSlotStatus.IN_PROGRESS -> {
                when (next) {
                    AppointmentSlotStatus.CANCELLED -> return true
                }
                return false
            }
            AppointmentSlotStatus.COMPLETED -> {
                when (next) {
                    AppointmentSlotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentSlotStatus) {
        if (!canMoveTo(next)) {
            error("appointment slot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"appointment slot"} $code"

    fun netLoad(): Double = round((etaMinutes.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): AppointmentSlot {
        return AppointmentSlot(
            code = code,
            title = title,
            limit = limit,
            shiftCode = shiftCode,
            batchSize = batchSize,
            updatedAt = updatedAt,
            visible = visible,
            currency = currency,
            createdAt = createdAt,
            etaMinutes = etaMinutes,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment slot in status planned. */
fun newAppointmentSlot(code: String, title: String): AppointmentSlot {
    return AppointmentSlot(
        code = code,
        title = title,
        limit = 0,
        shiftCode = "",
        batchSize = 0,
        updatedAt = 0,
        visible = false,
        currency = "",
        createdAt = 0,
        etaMinutes = 0,
        checksum = "",
        status = AppointmentSlotStatus.PLANNED,
    )
}
