package com.acme.transport.freightpay.freightinvoice.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice checkpoint is in its life cycle. */
enum class FreightInvoiceCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a freight invoice checkpoint and its current status. */
data class FreightInvoiceCheckpoint(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var stopCount: Int,
    var version: Int,
    var revision: Int,
    var source: String,
    var status: FreightInvoiceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceCheckpointStatus): Boolean {
        when (status) {
            FreightInvoiceCheckpointStatus.PLANNED -> {
                when (next) {
                    FreightInvoiceCheckpointStatus.IN_PROGRESS, FreightInvoiceCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            FreightInvoiceCheckpointStatus.SCHEDULED -> return next == FreightInvoiceCheckpointStatus.IN_PROGRESS || next == FreightInvoiceCheckpointStatus.COMPLETED
            FreightInvoiceCheckpointStatus.IN_PROGRESS -> {
                when (next) {
                    FreightInvoiceCheckpointStatus.COMPLETED, FreightInvoiceCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            FreightInvoiceCheckpointStatus.COMPLETED -> {
                when (next) {
                    FreightInvoiceCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((stopCount.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceCheckpoint {
        return FreightInvoiceCheckpoint(
            code = code,
            name = name,
            pinned = pinned,
            stopCount = stopCount,
            version = version,
            revision = revision,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a freight invoice checkpoint in status planned. */
fun newFreightInvoiceCheckpoint(code: String, name: String): FreightInvoiceCheckpoint {
    return FreightInvoiceCheckpoint(
        code = code,
        name = name,
        pinned = false,
        stopCount = 0,
        version = 0,
        revision = 0,
        source = "",
        status = FreightInvoiceCheckpointStatus.PLANNED,
    )
}
