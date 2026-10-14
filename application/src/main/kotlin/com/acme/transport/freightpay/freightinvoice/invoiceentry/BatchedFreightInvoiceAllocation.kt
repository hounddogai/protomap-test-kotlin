package com.acme.transport.freightpay.freightinvoice.invoiceentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched freight invoice allocation is in its life cycle. */
enum class BatchedFreightInvoiceAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A batched freight invoice allocation as the freight invoice team keeps it. */
data class BatchedFreightInvoiceAllocation(
    var code: String,
    var label: String,
    var version: Int,
    var retries: Int,
    var trailerId: String,
    var dock: String,
    var status: BatchedFreightInvoiceAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFreightInvoiceAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFreightInvoiceAllocationStatus): Boolean {
        when (status) {
            BatchedFreightInvoiceAllocationStatus.REQUESTED -> return next == BatchedFreightInvoiceAllocationStatus.EXPIRED
            BatchedFreightInvoiceAllocationStatus.GRANTED -> {
                when (next) {
                    BatchedFreightInvoiceAllocationStatus.REVOKED, BatchedFreightInvoiceAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedFreightInvoiceAllocationStatus.REVOKED -> {
                when (next) {
                    BatchedFreightInvoiceAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedFreightInvoiceAllocationStatus) {
        if (!canMoveTo(next)) {
            error("batched freight invoice allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round(retries.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): BatchedFreightInvoiceAllocation {
        return BatchedFreightInvoiceAllocation(
            code = code,
            label = label,
            version = version,
            retries = retries,
            trailerId = trailerId,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched freight invoice allocation in status requested. */
fun newBatchedFreightInvoiceAllocation(code: String, label: String): BatchedFreightInvoiceAllocation {
    return BatchedFreightInvoiceAllocation(
        code = code,
        label = label,
        version = 0,
        retries = 0,
        trailerId = "",
        dock = "",
        status = BatchedFreightInvoiceAllocationStatus.REQUESTED,
    )
}
