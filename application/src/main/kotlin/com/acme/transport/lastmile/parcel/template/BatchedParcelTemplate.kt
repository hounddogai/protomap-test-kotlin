package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched parcel template records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a batched parcel template is in its life cycle. */
enum class BatchedParcelTemplateStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched parcel template and its current status. */
data class BatchedParcelTemplate(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var retries: Int,
    var routeCode: String,
    var etaMinutes: Int,
    var revision: Int,
    var title: String,
    var status: BatchedParcelTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedParcelTemplateStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedParcelTemplateStatus): Boolean {
        when (status) {
            BatchedParcelTemplateStatus.REQUESTED -> return next == BatchedParcelTemplateStatus.GRANTED
            BatchedParcelTemplateStatus.GRANTED -> {
                when (next) {
                    BatchedParcelTemplateStatus.REVOKED, BatchedParcelTemplateStatus.EXPIRED -> return true
                }
                return false
            }
            BatchedParcelTemplateStatus.REVOKED -> return next == BatchedParcelTemplateStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: BatchedParcelTemplateStatus) {
        if (!canMoveTo(next)) {
            error("batched parcel template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"batched parcel template"} $code"

    fun adjustedLoad(): Double = round((revision.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): BatchedParcelTemplate {
        return BatchedParcelTemplate(
            code = code,
            label = label,
            updatedAt = updatedAt,
            retries = retries,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            revision = revision,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched parcel template in status requested. */
fun newBatchedParcelTemplate(code: String, label: String): BatchedParcelTemplate {
    return BatchedParcelTemplate(
        code = code,
        label = label,
        updatedAt = 0,
        retries = 0,
        routeCode = "",
        etaMinutes = 0,
        revision = 0,
        title = "",
        status = BatchedParcelTemplateStatus.REQUESTED,
    )
}
