package com.acme.transport.freightpay.audit.batch

import com.acme.transport.kit.text.normalize

// The global audit batch records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a global audit batch is in its life cycle. */
enum class GlobalAuditBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A global audit batch. Its code is unique within a tenant. */
data class GlobalAuditBatch(
    var code: String,
    var label: String,
    var reviewer: String,
    var attributes: MutableMap<String, String>,
    var country: String,
    var currency: String,
    var capacity: Int,
    var region: String,
    var status: GlobalAuditBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAuditBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAuditBatchStatus): Boolean {
        when (status) {
            GlobalAuditBatchStatus.NEW -> return next == GlobalAuditBatchStatus.RUNNING
            GlobalAuditBatchStatus.QUEUED -> {
                when (next) {
                    GlobalAuditBatchStatus.DONE, GlobalAuditBatchStatus.FAILED -> return true
                }
                return false
            }
            GlobalAuditBatchStatus.RUNNING -> return next == GlobalAuditBatchStatus.DONE
            GlobalAuditBatchStatus.DONE -> return next == GlobalAuditBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: GlobalAuditBatchStatus) {
        if (!canMoveTo(next)) {
            error("global audit batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global audit batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): GlobalAuditBatch {
        return GlobalAuditBatch(
            code = code,
            label = label,
            reviewer = reviewer,
            attributes = attributes,
            country = country,
            currency = currency,
            capacity = capacity,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global audit batch in status new. */
fun newGlobalAuditBatch(code: String, label: String): GlobalAuditBatch {
    return GlobalAuditBatch(
        code = code,
        label = label,
        reviewer = "",
        attributes = mutableMapOf<String, String>(),
        country = "",
        currency = "",
        capacity = 0,
        region = "",
        status = GlobalAuditBatchStatus.NEW,
    )
}
