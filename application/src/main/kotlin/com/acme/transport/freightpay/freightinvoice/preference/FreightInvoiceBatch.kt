package com.acme.transport.freightpay.freightinvoice.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice batch is in its life cycle. */
enum class FreightInvoiceBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One freight invoice batch, with what the planners need to act on it. */
data class FreightInvoiceBatch(
    var code: String,
    var title: String,
    var createdAt: Int,
    var phone: String,
    var ownerId: String,
    var durationMinutes: Int,
    var minimum: Double,
    var updatedAt: Int,
    var status: FreightInvoiceBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceBatchStatus): Boolean {
        when (status) {
            FreightInvoiceBatchStatus.REQUESTED -> return next == FreightInvoiceBatchStatus.REVOKED || next == FreightInvoiceBatchStatus.EXPIRED
            FreightInvoiceBatchStatus.GRANTED -> {
                when (next) {
                    FreightInvoiceBatchStatus.REVOKED -> return true
                }
                return false
            }
            FreightInvoiceBatchStatus.REVOKED -> {
                when (next) {
                    FreightInvoiceBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceBatchStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice batch $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netVolume(): Double = round(createdAt.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): FreightInvoiceBatch {
        return FreightInvoiceBatch(
            code = code,
            title = title,
            createdAt = createdAt,
            phone = phone,
            ownerId = ownerId,
            durationMinutes = durationMinutes,
            minimum = minimum,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a freight invoice batch in status requested. */
fun newFreightInvoiceBatch(code: String, title: String): FreightInvoiceBatch {
    return FreightInvoiceBatch(
        code = code,
        title = title,
        createdAt = 0,
        phone = "",
        ownerId = "",
        durationMinutes = 0,
        minimum = 0.0,
        updatedAt = 0,
        status = FreightInvoiceBatchStatus.REQUESTED,
    )
}
