package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect batch is in its life cycle. */
enum class DefectBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One defect batch, with what the planners need to act on it. */
data class DefectBatch(
    var code: String,
    var name: String,
    var weight: Double,
    var budget: Double,
    var country: String,
    var routeCode: String,
    var score: Double,
    var attributes: MutableMap<String, String>,
    var status: DefectBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectBatchStatus): Boolean {
        when (status) {
            DefectBatchStatus.CREATED -> {
                when (next) {
                    DefectBatchStatus.RETIRED -> return true
                }
                return false
            }
            DefectBatchStatus.VALIDATED -> return next == DefectBatchStatus.PUBLISHED
            DefectBatchStatus.PUBLISHED -> return next == DefectBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefectBatchStatus) {
        if (!canMoveTo(next)) {
            error("defect batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedValue(): Double = round((score + budget) * 100) / 100

    fun snapshot(): DefectBatch {
        return DefectBatch(
            code = code,
            name = name,
            weight = weight,
            budget = budget,
            country = country,
            routeCode = routeCode,
            score = score,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a defect batch in status created. */
fun newDefectBatch(code: String, name: String): DefectBatch {
    return DefectBatch(
        code = code,
        name = name,
        weight = 0.0,
        budget = 0.0,
        country = "",
        routeCode = "",
        score = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = DefectBatchStatus.CREATED,
    )
}
