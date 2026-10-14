package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize

// The batched duty period agreement records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a batched duty period agreement is in its life cycle. */
enum class BatchedDutyPeriodAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched duty period agreement as the duty period team keeps it. */
data class BatchedDutyPeriodAgreement(
    var code: String,
    var title: String,
    var budget: Double,
    var pinned: Boolean,
    var attributes: MutableMap<String, String>,
    var approved: Boolean,
    var status: BatchedDutyPeriodAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDutyPeriodAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDutyPeriodAgreementStatus): Boolean {
        when (status) {
            BatchedDutyPeriodAgreementStatus.NEW -> {
                when (next) {
                    BatchedDutyPeriodAgreementStatus.QUEUED, BatchedDutyPeriodAgreementStatus.RUNNING -> return true
                }
                return false
            }
            BatchedDutyPeriodAgreementStatus.QUEUED -> {
                when (next) {
                    BatchedDutyPeriodAgreementStatus.RUNNING, BatchedDutyPeriodAgreementStatus.FAILED -> return true
                }
                return false
            }
            BatchedDutyPeriodAgreementStatus.RUNNING -> return next == BatchedDutyPeriodAgreementStatus.DONE || next == BatchedDutyPeriodAgreementStatus.FAILED
            BatchedDutyPeriodAgreementStatus.DONE -> {
                when (next) {
                    BatchedDutyPeriodAgreementStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDutyPeriodAgreementStatus) {
        if (!canMoveTo(next)) {
            error("batched duty period agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = budget

    fun snapshot(): BatchedDutyPeriodAgreement {
        return BatchedDutyPeriodAgreement(
            code = code,
            title = title,
            budget = budget,
            pinned = pinned,
            attributes = attributes,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched duty period agreement in status new. */
fun newBatchedDutyPeriodAgreement(code: String, title: String): BatchedDutyPeriodAgreement {
    return BatchedDutyPeriodAgreement(
        code = code,
        title = title,
        budget = 0.0,
        pinned = false,
        attributes = mutableMapOf<String, String>(),
        approved = false,
        status = BatchedDutyPeriodAgreementStatus.NEW,
    )
}
