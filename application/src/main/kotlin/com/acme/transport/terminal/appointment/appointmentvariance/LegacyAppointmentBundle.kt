package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy appointment bundle is in its life cycle. */
enum class LegacyAppointmentBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One legacy appointment bundle, with what the planners need to act on it. */
data class LegacyAppointmentBundle(
    var code: String,
    var name: String,
    var region: String,
    var startsAt: Int,
    var tags: MutableList<String>,
    var pallets: Int,
    var status: LegacyAppointmentBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAppointmentBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAppointmentBundleStatus): Boolean {
        when (status) {
            LegacyAppointmentBundleStatus.IDLE -> return next == LegacyAppointmentBundleStatus.WARMING || next == LegacyAppointmentBundleStatus.STOPPED
            LegacyAppointmentBundleStatus.WARMING -> return next == LegacyAppointmentBundleStatus.READY
            LegacyAppointmentBundleStatus.READY -> {
                when (next) {
                    LegacyAppointmentBundleStatus.DRAINING, LegacyAppointmentBundleStatus.STOPPED -> return true
                }
                return false
            }
            LegacyAppointmentBundleStatus.DRAINING -> {
                when (next) {
                    LegacyAppointmentBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyAppointmentBundleStatus) {
        if (!canMoveTo(next)) {
            error("legacy appointment bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveVolume(): Double = round((pallets.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): LegacyAppointmentBundle {
        return LegacyAppointmentBundle(
            code = code,
            name = name,
            region = region,
            startsAt = startsAt,
            tags = tags,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a legacy appointment bundle in status idle. */
fun newLegacyAppointmentBundle(code: String, name: String): LegacyAppointmentBundle {
    return LegacyAppointmentBundle(
        code = code,
        name = name,
        region = "",
        startsAt = 0,
        tags = mutableListOf<String>(),
        pallets = 0,
        status = LegacyAppointmentBundleStatus.IDLE,
    )
}
