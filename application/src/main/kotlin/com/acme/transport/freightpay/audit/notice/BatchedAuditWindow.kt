package com.acme.transport.freightpay.audit.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched audit window is in its life cycle. */
enum class BatchedAuditWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched audit window as the audit team keeps it. */
data class BatchedAuditWindow(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var loadKg: Double,
    var checksum: String,
    var ownerId: String,
    var summary: String,
    var quantity: Int,
    var driverId: String,
    var odometer: Int,
    var status: BatchedAuditWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAuditWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAuditWindowStatus): Boolean {
        when (status) {
            BatchedAuditWindowStatus.NEW -> {
                when (next) {
                    BatchedAuditWindowStatus.QUEUED -> return true
                }
                return false
            }
            BatchedAuditWindowStatus.QUEUED -> {
                when (next) {
                    BatchedAuditWindowStatus.RUNNING, BatchedAuditWindowStatus.DONE -> return true
                }
                return false
            }
            BatchedAuditWindowStatus.RUNNING -> {
                when (next) {
                    BatchedAuditWindowStatus.FAILED -> return true
                }
                return false
            }
            BatchedAuditWindowStatus.DONE -> {
                when (next) {
                    BatchedAuditWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAuditWindowStatus) {
        if (!canMoveTo(next)) {
            error("batched audit window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched audit window"} $code"

    fun projectedVolume(): Double = round((fuelLiters - loadKg) * 100) / 100

    fun snapshot(): BatchedAuditWindow {
        return BatchedAuditWindow(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            loadKg = loadKg,
            checksum = checksum,
            ownerId = ownerId,
            summary = summary,
            quantity = quantity,
            driverId = driverId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched audit window in status new. */
fun newBatchedAuditWindow(code: String, title: String): BatchedAuditWindow {
    return BatchedAuditWindow(
        code = code,
        title = title,
        fuelLiters = 0.0,
        loadKg = 0.0,
        checksum = "",
        ownerId = "",
        summary = "",
        quantity = 0,
        driverId = "",
        odometer = 0,
        status = BatchedAuditWindowStatus.NEW,
    )
}
