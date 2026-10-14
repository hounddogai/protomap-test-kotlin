package com.acme.transport.freightpay.freightinvoice.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback freight invoice checkpoint is in its life cycle. */
enum class FallbackFreightInvoiceCheckpointStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback freight invoice checkpoint. Its code is unique within a tenant. */
data class FallbackFreightInvoiceCheckpoint(
    var code: String,
    var name: String,
    var reviewer: String,
    var contactEmail: String,
    var createdAt: Int,
    var endsAt: Int,
    var graceDays: Int,
    var source: String,
    var expiresAt: Int,
    var status: FallbackFreightInvoiceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackFreightInvoiceCheckpointStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackFreightInvoiceCheckpointStatus): Boolean {
        when (status) {
            FallbackFreightInvoiceCheckpointStatus.NEW -> return next == FallbackFreightInvoiceCheckpointStatus.RUNNING || next == FallbackFreightInvoiceCheckpointStatus.FAILED
            FallbackFreightInvoiceCheckpointStatus.QUEUED -> return next == FallbackFreightInvoiceCheckpointStatus.FAILED
            FallbackFreightInvoiceCheckpointStatus.RUNNING -> return next == FallbackFreightInvoiceCheckpointStatus.DONE
            FallbackFreightInvoiceCheckpointStatus.DONE -> return next == FallbackFreightInvoiceCheckpointStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackFreightInvoiceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("fallback freight invoice checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback freight invoice checkpoint"} $code"

    fun effectiveValue(): Double = round(endsAt.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): FallbackFreightInvoiceCheckpoint {
        return FallbackFreightInvoiceCheckpoint(
            code = code,
            name = name,
            reviewer = reviewer,
            contactEmail = contactEmail,
            createdAt = createdAt,
            endsAt = endsAt,
            graceDays = graceDays,
            source = source,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fallback freight invoice checkpoint in status new. */
fun newFallbackFreightInvoiceCheckpoint(code: String, name: String): FallbackFreightInvoiceCheckpoint {
    return FallbackFreightInvoiceCheckpoint(
        code = code,
        name = name,
        reviewer = "",
        contactEmail = "",
        createdAt = 0,
        endsAt = 0,
        graceDays = 0,
        source = "",
        expiresAt = 0,
        status = FallbackFreightInvoiceCheckpointStatus.NEW,
    )
}
