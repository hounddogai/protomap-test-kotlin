package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending freight invoice manifest is in its life cycle. */
enum class PendingFreightInvoiceManifestStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a pending freight invoice manifest and its current status. */
data class PendingFreightInvoiceManifest(
    var code: String,
    var label: String,
    var revision: Int,
    var notes: String,
    var externalRef: String,
    var durationMinutes: Int,
    var status: PendingFreightInvoiceManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFreightInvoiceManifestStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFreightInvoiceManifestStatus): Boolean {
        when (status) {
            PendingFreightInvoiceManifestStatus.NEW -> {
                when (next) {
                    PendingFreightInvoiceManifestStatus.QUEUED, PendingFreightInvoiceManifestStatus.RUNNING -> return true
                }
                return false
            }
            PendingFreightInvoiceManifestStatus.QUEUED -> {
                when (next) {
                    PendingFreightInvoiceManifestStatus.FAILED -> return true
                }
                return false
            }
            PendingFreightInvoiceManifestStatus.RUNNING -> return next == PendingFreightInvoiceManifestStatus.FAILED
            PendingFreightInvoiceManifestStatus.DONE -> {
                when (next) {
                    PendingFreightInvoiceManifestStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFreightInvoiceManifestStatus) {
        if (!canMoveTo(next)) {
            error("pending freight invoice manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round((revision.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingFreightInvoiceManifest {
        return PendingFreightInvoiceManifest(
            code = code,
            label = label,
            revision = revision,
            notes = notes,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending freight invoice manifest in status new. */
fun newPendingFreightInvoiceManifest(code: String, label: String): PendingFreightInvoiceManifest {
    return PendingFreightInvoiceManifest(
        code = code,
        label = label,
        revision = 0,
        notes = "",
        externalRef = "",
        durationMinutes = 0,
        status = PendingFreightInvoiceManifestStatus.NEW,
    )
}
