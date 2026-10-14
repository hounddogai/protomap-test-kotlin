package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched roster audit is in its life cycle. */
enum class BatchedRosterAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched roster audit. Its code is unique within a tenant. */
data class BatchedRosterAudit(
    var code: String,
    var label: String,
    var weight: Double,
    var score: Double,
    var ratio: Double,
    var carrier: String,
    var visible: Boolean,
    var currency: String,
    var fuelLiters: Double,
    var notes: String,
    var driverId: String,
    var status: BatchedRosterAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterAuditStatus): Boolean {
        when (status) {
            BatchedRosterAuditStatus.NEW -> {
                when (next) {
                    BatchedRosterAuditStatus.FAILED -> return true
                }
                return false
            }
            BatchedRosterAuditStatus.QUEUED -> return next == BatchedRosterAuditStatus.DONE || next == BatchedRosterAuditStatus.FAILED
            BatchedRosterAuditStatus.RUNNING -> return next == BatchedRosterAuditStatus.DONE
            BatchedRosterAuditStatus.DONE -> {
                when (next) {
                    BatchedRosterAuditStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRosterAuditStatus) {
        if (!canMoveTo(next)) {
            error("batched roster audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched roster audit"} $code"

    fun weightedValue(): Double = round(score * fuelLiters * 100) / 100

    fun snapshot(): BatchedRosterAudit {
        return BatchedRosterAudit(
            code = code,
            label = label,
            weight = weight,
            score = score,
            ratio = ratio,
            carrier = carrier,
            visible = visible,
            currency = currency,
            fuelLiters = fuelLiters,
            notes = notes,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched roster audit in status new. */
fun newBatchedRosterAudit(code: String, label: String): BatchedRosterAudit {
    return BatchedRosterAudit(
        code = code,
        label = label,
        weight = 0.0,
        score = 0.0,
        ratio = 0.0,
        carrier = "",
        visible = false,
        currency = "",
        fuelLiters = 0.0,
        notes = "",
        driverId = "",
        status = BatchedRosterAuditStatus.NEW,
    )
}
