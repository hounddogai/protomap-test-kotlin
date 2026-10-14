package com.acme.transport.freightpay.freightinvoice.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice snapshot is in its life cycle. */
enum class FreightInvoiceSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A freight invoice snapshot. Its code is unique within a tenant. */
data class FreightInvoiceSnapshot(
    var code: String,
    var name: String,
    var discount: Double,
    var etaMinutes: Int,
    var stopCount: Int,
    var reviewer: String,
    var summary: String,
    var startsAt: Int,
    var status: FreightInvoiceSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceSnapshotStatus): Boolean {
        when (status) {
            FreightInvoiceSnapshotStatus.PROPOSED -> {
                when (next) {
                    FreightInvoiceSnapshotStatus.APPROVED -> return true
                }
                return false
            }
            FreightInvoiceSnapshotStatus.APPROVED -> return next == FreightInvoiceSnapshotStatus.REJECTED || next == FreightInvoiceSnapshotStatus.WITHDRAWN
            FreightInvoiceSnapshotStatus.REJECTED -> {
                when (next) {
                    FreightInvoiceSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedTotal(): Double = round(discount * stopCount.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceSnapshot {
        return FreightInvoiceSnapshot(
            code = code,
            name = name,
            discount = discount,
            etaMinutes = etaMinutes,
            stopCount = stopCount,
            reviewer = reviewer,
            summary = summary,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a freight invoice snapshot in status proposed. */
fun newFreightInvoiceSnapshot(code: String, name: String): FreightInvoiceSnapshot {
    return FreightInvoiceSnapshot(
        code = code,
        name = name,
        discount = 0.0,
        etaMinutes = 0,
        stopCount = 0,
        reviewer = "",
        summary = "",
        startsAt = 0,
        status = FreightInvoiceSnapshotStatus.PROPOSED,
    )
}
