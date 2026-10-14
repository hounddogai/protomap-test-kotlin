package com.acme.transport.customs.inspection.metric

import com.acme.transport.kit.text.normalize

/** Where a default inspection estimate is in its life cycle. */
enum class DefaultInspectionEstimateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One default inspection estimate, with what the planners need to act on it. */
data class DefaultInspectionEstimate(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var depot: String,
    var stopCount: Int,
    var status: DefaultInspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultInspectionEstimateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultInspectionEstimateStatus): Boolean {
        when (status) {
            DefaultInspectionEstimateStatus.PLANNED -> return next == DefaultInspectionEstimateStatus.SCHEDULED || next == DefaultInspectionEstimateStatus.COMPLETED
            DefaultInspectionEstimateStatus.SCHEDULED -> {
                when (next) {
                    DefaultInspectionEstimateStatus.IN_PROGRESS -> return true
                }
                return false
            }
            DefaultInspectionEstimateStatus.IN_PROGRESS -> return next == DefaultInspectionEstimateStatus.CANCELLED
            DefaultInspectionEstimateStatus.COMPLETED -> return next == DefaultInspectionEstimateStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DefaultInspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("default inspection estimate $code cannot move from $status to $next")
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

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): DefaultInspectionEstimate {
        return DefaultInspectionEstimate(
            code = code,
            name = name,
            attributes = attributes,
            depot = depot,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default inspection estimate in status planned. */
fun newDefaultInspectionEstimate(code: String, name: String): DefaultInspectionEstimate {
    return DefaultInspectionEstimate(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        depot = "",
        stopCount = 0,
        status = DefaultInspectionEstimateStatus.PLANNED,
    )
}
