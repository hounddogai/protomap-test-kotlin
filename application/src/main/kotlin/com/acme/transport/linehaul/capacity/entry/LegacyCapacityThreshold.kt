package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy capacity threshold is in its life cycle. */
enum class LegacyCapacityThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a legacy capacity threshold and its current status. */
data class LegacyCapacityThreshold(
    var code: String,
    var name: String,
    var batchSize: Int,
    var threshold: Double,
    var margin: Double,
    var dock: String,
    var externalRef: String,
    var status: LegacyCapacityThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCapacityThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCapacityThresholdStatus): Boolean {
        when (status) {
            LegacyCapacityThresholdStatus.PROPOSED -> return next == LegacyCapacityThresholdStatus.APPROVED
            LegacyCapacityThresholdStatus.APPROVED -> return next == LegacyCapacityThresholdStatus.REJECTED || next == LegacyCapacityThresholdStatus.WITHDRAWN
            LegacyCapacityThresholdStatus.REJECTED -> return next == LegacyCapacityThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyCapacityThresholdStatus) {
        if (!canMoveTo(next)) {
            error("legacy capacity threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveExposure(): Double = round(threshold * margin * 100) / 100

    fun snapshot(): LegacyCapacityThreshold {
        return LegacyCapacityThreshold(
            code = code,
            name = name,
            batchSize = batchSize,
            threshold = threshold,
            margin = margin,
            dock = dock,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy capacity threshold in status proposed. */
fun newLegacyCapacityThreshold(code: String, name: String): LegacyCapacityThreshold {
    return LegacyCapacityThreshold(
        code = code,
        name = name,
        batchSize = 0,
        threshold = 0.0,
        margin = 0.0,
        dock = "",
        externalRef = "",
        status = LegacyCapacityThresholdStatus.PROPOSED,
    )
}
