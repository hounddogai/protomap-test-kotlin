package com.acme.transport.lastmile.locker.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a locker estimate is in its life cycle. */
enum class LockerEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A locker estimate. Its code is unique within a tenant. */
data class LockerEstimate(
    var code: String,
    var label: String,
    var budget: Double,
    var batchSize: Int,
    var reviewer: String,
    var windowDays: Int,
    var status: LockerEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LockerEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LockerEstimateStatus): Boolean {
        when (status) {
            LockerEstimateStatus.CREATED -> return next == LockerEstimateStatus.VALIDATED || next == LockerEstimateStatus.RETIRED
            LockerEstimateStatus.VALIDATED -> {
                when (next) {
                    LockerEstimateStatus.PUBLISHED, LockerEstimateStatus.RETIRED -> return true
                }
                return false
            }
            LockerEstimateStatus.PUBLISHED -> {
                when (next) {
                    LockerEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LockerEstimateStatus) {
        if (!canMoveTo(next)) {
            error("locker estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedValue(): Double = round((budget - batchSize.toDouble()) * 100) / 100

    fun snapshot(): LockerEstimate {
        return LockerEstimate(
            code = code,
            label = label,
            budget = budget,
            batchSize = batchSize,
            reviewer = reviewer,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a locker estimate in status created. */
fun newLockerEstimate(code: String, label: String): LockerEstimate {
    return LockerEstimate(
        code = code,
        label = label,
        budget = 0.0,
        batchSize = 0,
        reviewer = "",
        windowDays = 0,
        status = LockerEstimateStatus.CREATED,
    )
}
