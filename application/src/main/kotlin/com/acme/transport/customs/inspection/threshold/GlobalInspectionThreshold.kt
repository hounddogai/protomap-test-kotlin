package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.text.normalize

// The global inspection threshold records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a global inspection threshold is in its life cycle. */
enum class GlobalInspectionThresholdStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global inspection threshold as the inspection team keeps it. */
data class GlobalInspectionThreshold(
    var code: String,
    var title: String,
    var lane: String,
    var depot: String,
    var region: String,
    var dock: String,
    var maximum: Double,
    var status: GlobalInspectionThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalInspectionThresholdStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalInspectionThresholdStatus): Boolean {
        when (status) {
            GlobalInspectionThresholdStatus.REQUESTED -> return next == GlobalInspectionThresholdStatus.GRANTED || next == GlobalInspectionThresholdStatus.REVOKED
            GlobalInspectionThresholdStatus.GRANTED -> {
                when (next) {
                    GlobalInspectionThresholdStatus.REVOKED -> return true
                }
                return false
            }
            GlobalInspectionThresholdStatus.REVOKED -> return next == GlobalInspectionThresholdStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalInspectionThresholdStatus) {
        if (!canMoveTo(next)) {
            error("global inspection threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = maximum

    fun snapshot(): GlobalInspectionThreshold {
        return GlobalInspectionThreshold(
            code = code,
            title = title,
            lane = lane,
            depot = depot,
            region = region,
            dock = dock,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global inspection threshold in status requested. */
fun newGlobalInspectionThreshold(code: String, title: String): GlobalInspectionThreshold {
    return GlobalInspectionThreshold(
        code = code,
        title = title,
        lane = "",
        depot = "",
        region = "",
        dock = "",
        maximum = 0.0,
        status = GlobalInspectionThresholdStatus.REQUESTED,
    )
}
