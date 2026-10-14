package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.normalize

/** Where a rolling tariff batch is in its life cycle. */
enum class RollingTariffBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One rolling tariff batch, with what the planners need to act on it. */
data class RollingTariffBatch(
    var code: String,
    var title: String,
    var reviewer: String,
    var pinned: Boolean,
    var distanceKm: Double,
    var notes: String,
    var status: RollingTariffBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTariffBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTariffBatchStatus): Boolean {
        when (status) {
            RollingTariffBatchStatus.OPEN -> return next == RollingTariffBatchStatus.IN_REVIEW || next == RollingTariffBatchStatus.ARCHIVED
            RollingTariffBatchStatus.IN_REVIEW -> {
                when (next) {
                    RollingTariffBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            RollingTariffBatchStatus.RESOLVED -> return next == RollingTariffBatchStatus.REOPENED
            RollingTariffBatchStatus.REOPENED -> return next == RollingTariffBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RollingTariffBatchStatus) {
        if (!canMoveTo(next)) {
            error("rolling tariff batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = distanceKm

    fun snapshot(): RollingTariffBatch {
        return RollingTariffBatch(
            code = code,
            title = title,
            reviewer = reviewer,
            pinned = pinned,
            distanceKm = distanceKm,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling tariff batch in status open. */
fun newRollingTariffBatch(code: String, title: String): RollingTariffBatch {
    return RollingTariffBatch(
        code = code,
        title = title,
        reviewer = "",
        pinned = false,
        distanceKm = 0.0,
        notes = "",
        status = RollingTariffBatchStatus.OPEN,
    )
}
