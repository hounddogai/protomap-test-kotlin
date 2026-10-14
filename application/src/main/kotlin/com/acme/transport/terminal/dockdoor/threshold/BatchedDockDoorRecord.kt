package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dock door record is in its life cycle. */
enum class BatchedDockDoorRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One batched dock door record, with what the planners need to act on it. */
data class BatchedDockDoorRecord(
    var code: String,
    var title: String,
    var label: String,
    var active: Boolean,
    var country: String,
    var ownerId: String,
    var fuelLiters: Double,
    var retries: Int,
    var margin: Double,
    var status: BatchedDockDoorRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDockDoorRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDockDoorRecordStatus): Boolean {
        when (status) {
            BatchedDockDoorRecordStatus.NEW -> return next == BatchedDockDoorRecordStatus.QUEUED || next == BatchedDockDoorRecordStatus.DONE
            BatchedDockDoorRecordStatus.QUEUED -> return next == BatchedDockDoorRecordStatus.DONE || next == BatchedDockDoorRecordStatus.FAILED
            BatchedDockDoorRecordStatus.RUNNING -> {
                when (next) {
                    BatchedDockDoorRecordStatus.DONE, BatchedDockDoorRecordStatus.FAILED -> return true
                }
                return false
            }
            BatchedDockDoorRecordStatus.DONE -> {
                when (next) {
                    BatchedDockDoorRecordStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDockDoorRecordStatus) {
        if (!canMoveTo(next)) {
            error("batched dock door record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched dock door record"} $code"

    fun adjustedExposure(): Double = round((retries.toDouble() + margin) * 100) / 100

    fun snapshot(): BatchedDockDoorRecord {
        return BatchedDockDoorRecord(
            code = code,
            title = title,
            label = label,
            active = active,
            country = country,
            ownerId = ownerId,
            fuelLiters = fuelLiters,
            retries = retries,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched dock door record in status new. */
fun newBatchedDockDoorRecord(code: String, title: String): BatchedDockDoorRecord {
    return BatchedDockDoorRecord(
        code = code,
        title = title,
        label = "",
        active = false,
        country = "",
        ownerId = "",
        fuelLiters = 0.0,
        retries = 0,
        margin = 0.0,
        status = BatchedDockDoorRecordStatus.NEW,
    )
}
