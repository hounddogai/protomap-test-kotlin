package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered freight invoice checkpoint is in its life cycle. */
enum class TieredFreightInvoiceCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered freight invoice checkpoint and its current status. */
data class TieredFreightInvoiceCheckpoint(
    var code: String,
    var name: String,
    var depot: String,
    var budget: Double,
    var label: String,
    var createdAt: Int,
    var visible: Boolean,
    var active: Boolean,
    var contactEmail: String,
    var stopCount: Int,
    var distanceKm: Double,
    var status: TieredFreightInvoiceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredFreightInvoiceCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredFreightInvoiceCheckpointStatus): Boolean {
        when (status) {
            TieredFreightInvoiceCheckpointStatus.PROPOSED -> return next == TieredFreightInvoiceCheckpointStatus.APPROVED
            TieredFreightInvoiceCheckpointStatus.APPROVED -> return next == TieredFreightInvoiceCheckpointStatus.REJECTED || next == TieredFreightInvoiceCheckpointStatus.WITHDRAWN
            TieredFreightInvoiceCheckpointStatus.REJECTED -> return next == TieredFreightInvoiceCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TieredFreightInvoiceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("tiered freight invoice checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun netLoad(): Double = round((stopCount.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): TieredFreightInvoiceCheckpoint {
        return TieredFreightInvoiceCheckpoint(
            code = code,
            name = name,
            depot = depot,
            budget = budget,
            label = label,
            createdAt = createdAt,
            visible = visible,
            active = active,
            contactEmail = contactEmail,
            stopCount = stopCount,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tiered freight invoice checkpoint in status proposed. */
fun newTieredFreightInvoiceCheckpoint(code: String, name: String): TieredFreightInvoiceCheckpoint {
    return TieredFreightInvoiceCheckpoint(
        code = code,
        name = name,
        depot = "",
        budget = 0.0,
        label = "",
        createdAt = 0,
        visible = false,
        active = false,
        contactEmail = "",
        stopCount = 0,
        distanceKm = 0.0,
        status = TieredFreightInvoiceCheckpointStatus.PROPOSED,
    )
}
