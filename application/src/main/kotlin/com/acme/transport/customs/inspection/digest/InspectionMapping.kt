package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection mapping is in its life cycle. */
enum class InspectionMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A inspection mapping as the inspection team keeps it. */
data class InspectionMapping(
    var code: String,
    var label: String,
    var score: Double,
    var phone: String,
    var retries: Int,
    var discount: Double,
    var rate: Double,
    var reviewer: String,
    var attempts: Int,
    var stopCount: Int,
    var tags: MutableList<String>,
    var status: InspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionMappingStatus): Boolean {
        when (status) {
            InspectionMappingStatus.PLANNED -> {
                when (next) {
                    InspectionMappingStatus.IN_PROGRESS, InspectionMappingStatus.COMPLETED -> return true
                }
                return false
            }
            InspectionMappingStatus.SCHEDULED -> {
                when (next) {
                    InspectionMappingStatus.COMPLETED, InspectionMappingStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionMappingStatus.IN_PROGRESS -> {
                when (next) {
                    InspectionMappingStatus.COMPLETED, InspectionMappingStatus.CANCELLED -> return true
                }
                return false
            }
            InspectionMappingStatus.COMPLETED -> {
                when (next) {
                    InspectionMappingStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("inspection mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round(score * discount * 100) / 100

    fun snapshot(): InspectionMapping {
        return InspectionMapping(
            code = code,
            label = label,
            score = score,
            phone = phone,
            retries = retries,
            discount = discount,
            rate = rate,
            reviewer = reviewer,
            attempts = attempts,
            stopCount = stopCount,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a inspection mapping in status planned. */
fun newInspectionMapping(code: String, label: String): InspectionMapping {
    return InspectionMapping(
        code = code,
        label = label,
        score = 0.0,
        phone = "",
        retries = 0,
        discount = 0.0,
        rate = 0.0,
        reviewer = "",
        attempts = 0,
        stopCount = 0,
        tags = mutableListOf<String>(),
        status = InspectionMappingStatus.PLANNED,
    )
}
