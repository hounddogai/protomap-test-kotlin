package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cross dock allocation is in its life cycle. */
enum class CrossDockAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A cross dock allocation. Its code is unique within a tenant. */
data class CrossDockAllocation(
    var code: String,
    var title: String,
    var floorValue: Double,
    var externalRef: String,
    var odometer: Int,
    var dock: String,
    var sequence: Int,
    var contactEmail: String,
    var quantity: Int,
    var threshold: Double,
    var status: CrossDockAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CrossDockAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CrossDockAllocationStatus): Boolean {
        when (status) {
            CrossDockAllocationStatus.REQUESTED -> return next == CrossDockAllocationStatus.EXPIRED
            CrossDockAllocationStatus.GRANTED -> {
                when (next) {
                    CrossDockAllocationStatus.EXPIRED -> return true
                }
                return false
            }
            CrossDockAllocationStatus.REVOKED -> return next == CrossDockAllocationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CrossDockAllocationStatus) {
        if (!canMoveTo(next)) {
            error("cross dock allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round((sequence.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): CrossDockAllocation {
        return CrossDockAllocation(
            code = code,
            title = title,
            floorValue = floorValue,
            externalRef = externalRef,
            odometer = odometer,
            dock = dock,
            sequence = sequence,
            contactEmail = contactEmail,
            quantity = quantity,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a cross dock allocation in status requested. */
fun newCrossDockAllocation(code: String, title: String): CrossDockAllocation {
    return CrossDockAllocation(
        code = code,
        title = title,
        floorValue = 0.0,
        externalRef = "",
        odometer = 0,
        dock = "",
        sequence = 0,
        contactEmail = "",
        quantity = 0,
        threshold = 0.0,
        status = CrossDockAllocationStatus.REQUESTED,
    )
}
