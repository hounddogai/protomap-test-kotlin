package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched roster agreement is in its life cycle. */
enum class BatchedRosterAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched roster agreement as the roster team keeps it. */
data class BatchedRosterAgreement(
    var code: String,
    var title: String,
    var driverId: String,
    var discount: Double,
    var minimum: Double,
    var status: BatchedRosterAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterAgreementStatus): Boolean {
        when (status) {
            BatchedRosterAgreementStatus.NEW -> return next == BatchedRosterAgreementStatus.QUEUED
            BatchedRosterAgreementStatus.QUEUED -> {
                when (next) {
                    BatchedRosterAgreementStatus.RUNNING, BatchedRosterAgreementStatus.FAILED -> return true
                }
                return false
            }
            BatchedRosterAgreementStatus.RUNNING -> {
                when (next) {
                    BatchedRosterAgreementStatus.DONE, BatchedRosterAgreementStatus.FAILED -> return true
                }
                return false
            }
            BatchedRosterAgreementStatus.DONE -> return next == BatchedRosterAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedRosterAgreementStatus) {
        if (!canMoveTo(next)) {
            error("batched roster agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedVolume(): Double = round((minimum - discount) * 100) / 100

    fun snapshot(): BatchedRosterAgreement {
        return BatchedRosterAgreement(
            code = code,
            title = title,
            driverId = driverId,
            discount = discount,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched roster agreement in status new. */
fun newBatchedRosterAgreement(code: String, title: String): BatchedRosterAgreement {
    return BatchedRosterAgreement(
        code = code,
        title = title,
        driverId = "",
        discount = 0.0,
        minimum = 0.0,
        status = BatchedRosterAgreementStatus.NEW,
    )
}
