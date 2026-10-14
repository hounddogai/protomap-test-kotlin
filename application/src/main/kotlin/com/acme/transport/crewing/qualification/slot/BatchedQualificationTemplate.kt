package com.acme.transport.crewing.qualification.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched qualification template is in its life cycle. */
enum class BatchedQualificationTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched qualification template and its current status. */
data class BatchedQualificationTemplate(
    var code: String,
    var title: String,
    var active: Boolean,
    var endsAt: Int,
    var ceiling: Double,
    var maximum: Double,
    var status: BatchedQualificationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationTemplateStatus): Boolean {
        when (status) {
            BatchedQualificationTemplateStatus.DRAFT -> {
                when (next) {
                    BatchedQualificationTemplateStatus.PENDING -> return true
                }
                return false
            }
            BatchedQualificationTemplateStatus.PENDING -> return next == BatchedQualificationTemplateStatus.SUSPENDED || next == BatchedQualificationTemplateStatus.CLOSED
            BatchedQualificationTemplateStatus.ACTIVE -> {
                when (next) {
                    BatchedQualificationTemplateStatus.SUSPENDED, BatchedQualificationTemplateStatus.CLOSED -> return true
                }
                return false
            }
            BatchedQualificationTemplateStatus.SUSPENDED -> return next == BatchedQualificationTemplateStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedQualificationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched qualification template"} $code"

    fun adjustedLoad(): Double = round(maximum * endsAt.toDouble() * 100) / 100

    fun snapshot(): BatchedQualificationTemplate {
        return BatchedQualificationTemplate(
            code = code,
            title = title,
            active = active,
            endsAt = endsAt,
            ceiling = ceiling,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched qualification template in status draft. */
fun newBatchedQualificationTemplate(code: String, title: String): BatchedQualificationTemplate {
    return BatchedQualificationTemplate(
        code = code,
        title = title,
        active = false,
        endsAt = 0,
        ceiling = 0.0,
        maximum = 0.0,
        status = BatchedQualificationTemplateStatus.DRAFT,
    )
}
