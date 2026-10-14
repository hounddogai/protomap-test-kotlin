package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment ledger is in its life cycle. */
enum class AppointmentLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A appointment ledger. Its code is unique within a tenant. */
data class AppointmentLedger(
    var code: String,
    var name: String,
    var country: String,
    var version: Int,
    var budget: Double,
    var status: AppointmentLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentLedgerStatus): Boolean {
        when (status) {
            AppointmentLedgerStatus.IDLE -> return next == AppointmentLedgerStatus.STOPPED
            AppointmentLedgerStatus.WARMING -> {
                when (next) {
                    AppointmentLedgerStatus.DRAINING -> return true
                }
                return false
            }
            AppointmentLedgerStatus.READY -> return next == AppointmentLedgerStatus.STOPPED
            AppointmentLedgerStatus.DRAINING -> return next == AppointmentLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AppointmentLedgerStatus) {
        if (!canMoveTo(next)) {
            error("appointment ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"appointment ledger"} $code"

    fun effectiveLoad(): Double = round(version.toDouble() * budget * 100) / 100

    fun snapshot(): AppointmentLedger {
        return AppointmentLedger(
            code = code,
            name = name,
            country = country,
            version = version,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a appointment ledger in status idle. */
fun newAppointmentLedger(code: String, name: String): AppointmentLedger {
    return AppointmentLedger(
        code = code,
        name = name,
        country = "",
        version = 0,
        budget = 0.0,
        status = AppointmentLedgerStatus.IDLE,
    )
}
