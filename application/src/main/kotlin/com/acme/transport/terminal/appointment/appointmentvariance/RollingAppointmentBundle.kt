package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling appointment bundle is in its life cycle. */
enum class RollingAppointmentBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rolling appointment bundle, with what the planners need to act on it. */
data class RollingAppointmentBundle(
    var code: String,
    var label: String,
    var depot: String,
    var score: Double,
    var updatedAt: Int,
    var ownerId: String,
    var quantity: Int,
    var dock: String,
    var checksum: String,
    var status: RollingAppointmentBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAppointmentBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAppointmentBundleStatus): Boolean {
        when (status) {
            RollingAppointmentBundleStatus.REQUESTED -> return next == RollingAppointmentBundleStatus.GRANTED || next == RollingAppointmentBundleStatus.EXPIRED
            RollingAppointmentBundleStatus.GRANTED -> return next == RollingAppointmentBundleStatus.REVOKED || next == RollingAppointmentBundleStatus.EXPIRED
            RollingAppointmentBundleStatus.REVOKED -> {
                when (next) {
                    RollingAppointmentBundleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAppointmentBundleStatus) {
        if (!canMoveTo(next)) {
            error("rolling appointment bundle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling appointment bundle"} $code"

    fun projectedLoad(): Double = round(updatedAt.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingAppointmentBundle {
        return RollingAppointmentBundle(
            code = code,
            label = label,
            depot = depot,
            score = score,
            updatedAt = updatedAt,
            ownerId = ownerId,
            quantity = quantity,
            dock = dock,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling appointment bundle in status requested. */
fun newRollingAppointmentBundle(code: String, label: String): RollingAppointmentBundle {
    return RollingAppointmentBundle(
        code = code,
        label = label,
        depot = "",
        score = 0.0,
        updatedAt = 0,
        ownerId = "",
        quantity = 0,
        dock = "",
        checksum = "",
        status = RollingAppointmentBundleStatus.REQUESTED,
    )
}
