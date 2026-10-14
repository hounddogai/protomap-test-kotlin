package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice threshold is in its life cycle. */
enum class FreightInvoiceThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A freight invoice threshold as the freight invoice team keeps it. */
data class FreightInvoiceThreshold(
    var code: String,
    var label: String,
    var score: Double,
    var description: String,
    var pallets: Int,
    var carrier: String,
    var expiresAt: Int,
    var stopCount: Int,
    var floorValue: Double,
    var status: FreightInvoiceThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceThresholdStatus): Boolean {
        when (status) {
            FreightInvoiceThresholdStatus.IDLE -> {
                when (next) {
                    FreightInvoiceThresholdStatus.WARMING, FreightInvoiceThresholdStatus.READY -> return true
                }
                return false
            }
            FreightInvoiceThresholdStatus.WARMING -> return next == FreightInvoiceThresholdStatus.READY
            FreightInvoiceThresholdStatus.READY -> {
                when (next) {
                    FreightInvoiceThresholdStatus.STOPPED -> return true
                }
                return false
            }
            FreightInvoiceThresholdStatus.DRAINING -> return next == FreightInvoiceThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FreightInvoiceThresholdStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((stopCount.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceThreshold {
        return FreightInvoiceThreshold(
            code = code,
            label = label,
            score = score,
            description = description,
            pallets = pallets,
            carrier = carrier,
            expiresAt = expiresAt,
            stopCount = stopCount,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a freight invoice threshold in status idle. */
fun newFreightInvoiceThreshold(code: String, label: String): FreightInvoiceThreshold {
    return FreightInvoiceThreshold(
        code = code,
        label = label,
        score = 0.0,
        description = "",
        pallets = 0,
        carrier = "",
        expiresAt = 0,
        stopCount = 0,
        floorValue = 0.0,
        status = FreightInvoiceThresholdStatus.IDLE,
    )
}
