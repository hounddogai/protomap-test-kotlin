package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched inspection template is in its life cycle. */
enum class BatchedInspectionTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a batched inspection template and its current status. */
data class BatchedInspectionTemplate(
    var code: String,
    var name: String,
    var margin: Double,
    var attempts: Int,
    var maximum: Double,
    var approved: Boolean,
    var active: Boolean,
    var driverId: String,
    var fuelLiters: Double,
    var externalRef: String,
    var startsAt: Int,
    var status: BatchedInspectionTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedInspectionTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedInspectionTemplateStatus): Boolean {
        when (status) {
            BatchedInspectionTemplateStatus.CREATED -> return next == BatchedInspectionTemplateStatus.PUBLISHED || next == BatchedInspectionTemplateStatus.RETIRED
            BatchedInspectionTemplateStatus.VALIDATED -> return next == BatchedInspectionTemplateStatus.PUBLISHED || next == BatchedInspectionTemplateStatus.RETIRED
            BatchedInspectionTemplateStatus.PUBLISHED -> return next == BatchedInspectionTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedInspectionTemplateStatus) {
        if (!canMoveTo(next)) {
            error("batched inspection template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round(margin * maximum * 100) / 100

    fun snapshot(): BatchedInspectionTemplate {
        return BatchedInspectionTemplate(
            code = code,
            name = name,
            margin = margin,
            attempts = attempts,
            maximum = maximum,
            approved = approved,
            active = active,
            driverId = driverId,
            fuelLiters = fuelLiters,
            externalRef = externalRef,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched inspection template in status created. */
fun newBatchedInspectionTemplate(code: String, name: String): BatchedInspectionTemplate {
    return BatchedInspectionTemplate(
        code = code,
        name = name,
        margin = 0.0,
        attempts = 0,
        maximum = 0.0,
        approved = false,
        active = false,
        driverId = "",
        fuelLiters = 0.0,
        externalRef = "",
        startsAt = 0,
        status = BatchedInspectionTemplateStatus.CREATED,
    )
}
