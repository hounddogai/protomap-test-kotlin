package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending freight invoice schedule is in its life cycle. */
enum class PendingFreightInvoiceScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A pending freight invoice schedule. Its code is unique within a tenant. */
data class PendingFreightInvoiceSchedule(
    var code: String,
    var name: String,
    var visible: Boolean,
    var sequence: Int,
    var graceDays: Int,
    var ownerId: String,
    var status: PendingFreightInvoiceScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFreightInvoiceScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFreightInvoiceScheduleStatus): Boolean {
        when (status) {
            PendingFreightInvoiceScheduleStatus.REQUESTED -> {
                when (next) {
                    PendingFreightInvoiceScheduleStatus.GRANTED, PendingFreightInvoiceScheduleStatus.REVOKED -> return true
                }
                return false
            }
            PendingFreightInvoiceScheduleStatus.GRANTED -> {
                when (next) {
                    PendingFreightInvoiceScheduleStatus.REVOKED, PendingFreightInvoiceScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            PendingFreightInvoiceScheduleStatus.REVOKED -> {
                when (next) {
                    PendingFreightInvoiceScheduleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFreightInvoiceScheduleStatus) {
        if (!canMoveTo(next)) {
            error("pending freight invoice schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((graceDays.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): PendingFreightInvoiceSchedule {
        return PendingFreightInvoiceSchedule(
            code = code,
            name = name,
            visible = visible,
            sequence = sequence,
            graceDays = graceDays,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending freight invoice schedule in status requested. */
fun newPendingFreightInvoiceSchedule(code: String, name: String): PendingFreightInvoiceSchedule {
    return PendingFreightInvoiceSchedule(
        code = code,
        name = name,
        visible = false,
        sequence = 0,
        graceDays = 0,
        ownerId = "",
        status = PendingFreightInvoiceScheduleStatus.REQUESTED,
    )
}
