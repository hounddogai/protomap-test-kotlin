package com.acme.transport.freightpay.freightinvoice.allocation

import com.acme.transport.kit.text.normalize

/** Where a manual freight invoice checkpoint is in its life cycle. */
enum class ManualFreightInvoiceCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a manual freight invoice checkpoint and its current status. */
data class ManualFreightInvoiceCheckpoint(
    var code: String,
    var name: String,
    var visible: Boolean,
    var pinned: Boolean,
    var carrier: String,
    var weight: Double,
    var status: ManualFreightInvoiceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualFreightInvoiceCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualFreightInvoiceCheckpointStatus): Boolean {
        when (status) {
            ManualFreightInvoiceCheckpointStatus.IDLE -> return next == ManualFreightInvoiceCheckpointStatus.WARMING || next == ManualFreightInvoiceCheckpointStatus.DRAINING
            ManualFreightInvoiceCheckpointStatus.WARMING -> {
                when (next) {
                    ManualFreightInvoiceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            ManualFreightInvoiceCheckpointStatus.READY -> return next == ManualFreightInvoiceCheckpointStatus.DRAINING || next == ManualFreightInvoiceCheckpointStatus.STOPPED
            ManualFreightInvoiceCheckpointStatus.DRAINING -> {
                when (next) {
                    ManualFreightInvoiceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualFreightInvoiceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("manual freight invoice checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = weight

    fun snapshot(): ManualFreightInvoiceCheckpoint {
        return ManualFreightInvoiceCheckpoint(
            code = code,
            name = name,
            visible = visible,
            pinned = pinned,
            carrier = carrier,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual freight invoice checkpoint in status idle. */
fun newManualFreightInvoiceCheckpoint(code: String, name: String): ManualFreightInvoiceCheckpoint {
    return ManualFreightInvoiceCheckpoint(
        code = code,
        name = name,
        visible = false,
        pinned = false,
        carrier = "",
        weight = 0.0,
        status = ManualFreightInvoiceCheckpointStatus.IDLE,
    )
}
