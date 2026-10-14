package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment cycle is in its life cycle. */
enum class AppointmentCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A appointment cycle. Its code is unique within a tenant. */
data class AppointmentCycle(
    var code: String,
    var name: String,
    var source: String,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var attempts: Int,
    var contactEmail: String,
    var pallets: Int,
    var visible: Boolean,
    var channel: String,
    var version: Int,
    var status: AppointmentCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentCycleStatus): Boolean {
        when (status) {
            AppointmentCycleStatus.IDLE -> return next == AppointmentCycleStatus.WARMING
            AppointmentCycleStatus.WARMING -> return next == AppointmentCycleStatus.DRAINING
            AppointmentCycleStatus.READY -> {
                when (next) {
                    AppointmentCycleStatus.DRAINING, AppointmentCycleStatus.STOPPED -> return true
                }
                return false
            }
            AppointmentCycleStatus.DRAINING -> {
                when (next) {
                    AppointmentCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentCycleStatus) {
        if (!canMoveTo(next)) {
            error("appointment cycle $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round((pallets.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): AppointmentCycle {
        return AppointmentCycle(
            code = code,
            name = name,
            source = source,
            amount = amount,
            attributes = attributes,
            attempts = attempts,
            contactEmail = contactEmail,
            pallets = pallets,
            visible = visible,
            channel = channel,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a appointment cycle in status idle. */
fun newAppointmentCycle(code: String, name: String): AppointmentCycle {
    return AppointmentCycle(
        code = code,
        name = name,
        source = "",
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        attempts = 0,
        contactEmail = "",
        pallets = 0,
        visible = false,
        channel = "",
        version = 0,
        status = AppointmentCycleStatus.IDLE,
    )
}
