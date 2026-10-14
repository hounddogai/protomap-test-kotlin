package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.kit.text.normalize

/** Where a scheduled freight invoice snapshot is in its life cycle. */
enum class ScheduledFreightInvoiceSnapshotStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled freight invoice snapshot, with what the planners need to act on it. */
data class ScheduledFreightInvoiceSnapshot(
    var code: String,
    var title: String,
    var country: String,
    var trailerId: String,
    var ownerId: String,
    var label: String,
    var driverId: String,
    var externalRef: String,
    var limit: Int,
    var status: ScheduledFreightInvoiceSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFreightInvoiceSnapshotStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFreightInvoiceSnapshotStatus): Boolean {
        when (status) {
            ScheduledFreightInvoiceSnapshotStatus.DRAFT -> return next == ScheduledFreightInvoiceSnapshotStatus.PENDING
            ScheduledFreightInvoiceSnapshotStatus.PENDING -> {
                when (next) {
                    ScheduledFreightInvoiceSnapshotStatus.ACTIVE, ScheduledFreightInvoiceSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledFreightInvoiceSnapshotStatus.ACTIVE -> {
                when (next) {
                    ScheduledFreightInvoiceSnapshotStatus.SUSPENDED, ScheduledFreightInvoiceSnapshotStatus.CLOSED -> return true
                }
                return false
            }
            ScheduledFreightInvoiceSnapshotStatus.SUSPENDED -> {
                when (next) {
                    ScheduledFreightInvoiceSnapshotStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledFreightInvoiceSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled freight invoice snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $title"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): ScheduledFreightInvoiceSnapshot {
        return ScheduledFreightInvoiceSnapshot(
            code = code,
            title = title,
            country = country,
            trailerId = trailerId,
            ownerId = ownerId,
            label = label,
            driverId = driverId,
            externalRef = externalRef,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled freight invoice snapshot in status draft. */
fun newScheduledFreightInvoiceSnapshot(code: String, title: String): ScheduledFreightInvoiceSnapshot {
    return ScheduledFreightInvoiceSnapshot(
        code = code,
        title = title,
        country = "",
        trailerId = "",
        ownerId = "",
        label = "",
        driverId = "",
        externalRef = "",
        limit = 0,
        status = ScheduledFreightInvoiceSnapshotStatus.DRAFT,
    )
}
