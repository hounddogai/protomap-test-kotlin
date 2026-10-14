package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect variance is in its life cycle. */
enum class DefectVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a defect variance and its current status. */
data class DefectVariance(
    var code: String,
    var label: String,
    var category: String,
    var checksum: String,
    var stopCount: Int,
    var version: Int,
    var description: String,
    var status: DefectVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectVarianceStatus): Boolean {
        when (status) {
            DefectVarianceStatus.CREATED -> return next == DefectVarianceStatus.VALIDATED || next == DefectVarianceStatus.RETIRED
            DefectVarianceStatus.VALIDATED -> return next == DefectVarianceStatus.PUBLISHED
            DefectVarianceStatus.PUBLISHED -> {
                when (next) {
                    DefectVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectVarianceStatus) {
        if (!canMoveTo(next)) {
            error("defect variance $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"defect variance"} $code"

    fun effectiveLoad(): Double = round((stopCount.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): DefectVariance {
        return DefectVariance(
            code = code,
            label = label,
            category = category,
            checksum = checksum,
            stopCount = stopCount,
            version = version,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a defect variance in status created. */
fun newDefectVariance(code: String, label: String): DefectVariance {
    return DefectVariance(
        code = code,
        label = label,
        category = "",
        checksum = "",
        stopCount = 0,
        version = 0,
        description = "",
        status = DefectVarianceStatus.CREATED,
    )
}
