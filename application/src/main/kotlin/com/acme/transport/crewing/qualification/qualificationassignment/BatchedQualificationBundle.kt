package com.acme.transport.crewing.qualification.qualificationassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched qualification bundle is in its life cycle. */
enum class BatchedQualificationBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched qualification bundle. Its code is unique within a tenant. */
data class BatchedQualificationBundle(
    var code: String,
    var label: String,
    var routeCode: String,
    var description: String,
    var weight: Double,
    var margin: Double,
    var region: String,
    var loadKg: Double,
    var etaMinutes: Int,
    var minimum: Double,
    var currency: String,
    var status: BatchedQualificationBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationBundleStatus): Boolean {
        when (status) {
            BatchedQualificationBundleStatus.CREATED -> return next == BatchedQualificationBundleStatus.VALIDATED || next == BatchedQualificationBundleStatus.PUBLISHED
            BatchedQualificationBundleStatus.VALIDATED -> return next == BatchedQualificationBundleStatus.PUBLISHED
            BatchedQualificationBundleStatus.PUBLISHED -> return next == BatchedQualificationBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedQualificationBundleStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round(loadKg * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): BatchedQualificationBundle {
        return BatchedQualificationBundle(
            code = code,
            label = label,
            routeCode = routeCode,
            description = description,
            weight = weight,
            margin = margin,
            region = region,
            loadKg = loadKg,
            etaMinutes = etaMinutes,
            minimum = minimum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched qualification bundle in status created. */
fun newBatchedQualificationBundle(code: String, label: String): BatchedQualificationBundle {
    return BatchedQualificationBundle(
        code = code,
        label = label,
        routeCode = "",
        description = "",
        weight = 0.0,
        margin = 0.0,
        region = "",
        loadKg = 0.0,
        etaMinutes = 0,
        minimum = 0.0,
        currency = "",
        status = BatchedQualificationBundleStatus.CREATED,
    )
}
