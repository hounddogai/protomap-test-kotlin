package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize

/** Where a batched capacity agreement is in its life cycle. */
enum class BatchedCapacityAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One batched capacity agreement, with what the planners need to act on it. */
data class BatchedCapacityAgreement(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var driverId: String,
    var active: Boolean,
    var status: BatchedCapacityAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityAgreementStatus): Boolean {
        when (status) {
            BatchedCapacityAgreementStatus.CREATED -> {
                when (next) {
                    BatchedCapacityAgreementStatus.PUBLISHED, BatchedCapacityAgreementStatus.RETIRED -> return true
                }
                return false
            }
            BatchedCapacityAgreementStatus.VALIDATED -> {
                when (next) {
                    BatchedCapacityAgreementStatus.PUBLISHED, BatchedCapacityAgreementStatus.RETIRED -> return true
                }
                return false
            }
            BatchedCapacityAgreementStatus.PUBLISHED -> return next == BatchedCapacityAgreementStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedCapacityAgreementStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): BatchedCapacityAgreement {
        return BatchedCapacityAgreement(
            code = code,
            title = title,
            distanceKm = distanceKm,
            driverId = driverId,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched capacity agreement in status created. */
fun newBatchedCapacityAgreement(code: String, title: String): BatchedCapacityAgreement {
    return BatchedCapacityAgreement(
        code = code,
        title = title,
        distanceKm = 0.0,
        driverId = "",
        active = false,
        status = BatchedCapacityAgreementStatus.CREATED,
    )
}
