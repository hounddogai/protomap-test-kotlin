package com.acme.transport.crewing.availability.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched availability agreement is in its life cycle. */
enum class BatchedAvailabilityAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched availability agreement as the availability team keeps it. */
data class BatchedAvailabilityAgreement(
    var code: String,
    var name: String,
    var depot: String,
    var budget: Double,
    var etaMinutes: Int,
    var shiftCode: String,
    var summary: String,
    var archived: Boolean,
    var region: String,
    var retries: Int,
    var description: String,
    var status: BatchedAvailabilityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAvailabilityAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAvailabilityAgreementStatus): Boolean {
        when (status) {
            BatchedAvailabilityAgreementStatus.PROPOSED -> return next == BatchedAvailabilityAgreementStatus.APPROVED || next == BatchedAvailabilityAgreementStatus.WITHDRAWN
            BatchedAvailabilityAgreementStatus.APPROVED -> {
                when (next) {
                    BatchedAvailabilityAgreementStatus.REJECTED -> return true
                }
                return false
            }
            BatchedAvailabilityAgreementStatus.REJECTED -> return next == BatchedAvailabilityAgreementStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedAvailabilityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("batched availability agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((budget - retries.toDouble()) * 100) / 100

    fun snapshot(): BatchedAvailabilityAgreement {
        return BatchedAvailabilityAgreement(
            code = code,
            name = name,
            depot = depot,
            budget = budget,
            etaMinutes = etaMinutes,
            shiftCode = shiftCode,
            summary = summary,
            archived = archived,
            region = region,
            retries = retries,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched availability agreement in status proposed. */
fun newBatchedAvailabilityAgreement(code: String, name: String): BatchedAvailabilityAgreement {
    return BatchedAvailabilityAgreement(
        code = code,
        name = name,
        depot = "",
        budget = 0.0,
        etaMinutes = 0,
        shiftCode = "",
        summary = "",
        archived = false,
        region = "",
        retries = 0,
        description = "",
        status = BatchedAvailabilityAgreementStatus.PROPOSED,
    )
}
