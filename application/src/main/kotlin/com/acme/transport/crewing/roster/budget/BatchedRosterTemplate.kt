package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched roster template is in its life cycle. */
enum class BatchedRosterTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched roster template. Its code is unique within a tenant. */
data class BatchedRosterTemplate(
    var code: String,
    var name: String,
    var version: Int,
    var amount: Double,
    var createdAt: Int,
    var status: BatchedRosterTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterTemplateStatus): Boolean {
        when (status) {
            BatchedRosterTemplateStatus.CREATED -> return next == BatchedRosterTemplateStatus.VALIDATED || next == BatchedRosterTemplateStatus.RETIRED
            BatchedRosterTemplateStatus.VALIDATED -> {
                when (next) {
                    BatchedRosterTemplateStatus.RETIRED -> return true
                }
                return false
            }
            BatchedRosterTemplateStatus.PUBLISHED -> {
                when (next) {
                    BatchedRosterTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRosterTemplateStatus) {
        if (!canMoveTo(next)) {
            error("batched roster template $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveLoad(): Double = round(amount * createdAt.toDouble() * 100) / 100

    fun snapshot(): BatchedRosterTemplate {
        return BatchedRosterTemplate(
            code = code,
            name = name,
            version = version,
            amount = amount,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched roster template in status created. */
fun newBatchedRosterTemplate(code: String, name: String): BatchedRosterTemplate {
    return BatchedRosterTemplate(
        code = code,
        name = name,
        version = 0,
        amount = 0.0,
        createdAt = 0,
        status = BatchedRosterTemplateStatus.CREATED,
    )
}
