package com.acme.transport.customs.inspection.audit

import com.acme.transport.kit.text.normalize

/** Where a global inspection estimate is in its life cycle. */
enum class GlobalInspectionEstimateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global inspection estimate and its current status. */
data class GlobalInspectionEstimate(
    var code: String,
    var title: String,
    var lane: String,
    var createdAt: Int,
    var dock: String,
    var status: GlobalInspectionEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionEstimateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionEstimateStatus): Boolean {
        when (status) {
            GlobalInspectionEstimateStatus.PROPOSED -> return next == GlobalInspectionEstimateStatus.APPROVED
            GlobalInspectionEstimateStatus.APPROVED -> {
                when (next) {
                    GlobalInspectionEstimateStatus.REJECTED -> return true
                }
                return false
            }
            GlobalInspectionEstimateStatus.REJECTED -> return next == GlobalInspectionEstimateStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalInspectionEstimateStatus) {
        if (!canMoveTo(next)) {
            error("global inspection estimate $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): GlobalInspectionEstimate {
        return GlobalInspectionEstimate(
            code = code,
            title = title,
            lane = lane,
            createdAt = createdAt,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global inspection estimate in status proposed. */
fun newGlobalInspectionEstimate(code: String, title: String): GlobalInspectionEstimate {
    return GlobalInspectionEstimate(
        code = code,
        title = title,
        lane = "",
        createdAt = 0,
        dock = "",
        status = GlobalInspectionEstimateStatus.PROPOSED,
    )
}
