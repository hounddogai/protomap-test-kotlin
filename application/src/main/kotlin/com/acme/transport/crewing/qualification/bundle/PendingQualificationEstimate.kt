package com.acme.transport.crewing.qualification.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending qualification estimate is in its life cycle. */
enum class PendingQualificationEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending qualification estimate. Its code is unique within a tenant. */
data class PendingQualificationEstimate(
    var code: String,
    var label: String,
    var quantity: Int,
    var minimum: Double,
    var discount: Double,
    var tags: MutableList<String>,
    var status: PendingQualificationEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingQualificationEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingQualificationEstimateStatus): Boolean {
        when (status) {
            PendingQualificationEstimateStatus.OPEN -> return next == PendingQualificationEstimateStatus.IN_REVIEW || next == PendingQualificationEstimateStatus.ARCHIVED
            PendingQualificationEstimateStatus.IN_REVIEW -> {
                when (next) {
                    PendingQualificationEstimateStatus.RESOLVED -> return true
                }
                return false
            }
            PendingQualificationEstimateStatus.RESOLVED -> {
                when (next) {
                    PendingQualificationEstimateStatus.REOPENED -> return true
                }
                return false
            }
            PendingQualificationEstimateStatus.REOPENED -> {
                when (next) {
                    PendingQualificationEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingQualificationEstimateStatus) {
        if (!canMoveTo(next)) {
            error("pending qualification estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"pending qualification estimate"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((minimum - discount) * 100) / 100

    fun snapshot(): PendingQualificationEstimate {
        return PendingQualificationEstimate(
            code = code,
            label = label,
            quantity = quantity,
            minimum = minimum,
            discount = discount,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a pending qualification estimate in status open. */
fun newPendingQualificationEstimate(code: String, label: String): PendingQualificationEstimate {
    return PendingQualificationEstimate(
        code = code,
        label = label,
        quantity = 0,
        minimum = 0.0,
        discount = 0.0,
        tags = mutableListOf<String>(),
        status = PendingQualificationEstimateStatus.OPEN,
    )
}
