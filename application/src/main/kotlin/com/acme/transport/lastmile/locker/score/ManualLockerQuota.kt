package com.acme.transport.lastmile.locker.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual locker quota is in its life cycle. */
enum class ManualLockerQuotaStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual locker quota and its current status. */
data class ManualLockerQuota(
    var code: String,
    var label: String,
    var driverId: String,
    var capacity: Int,
    var depot: String,
    var durationMinutes: Int,
    var minimum: Double,
    var budget: Double,
    var status: ManualLockerQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualLockerQuotaStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualLockerQuotaStatus): Boolean {
        when (status) {
            ManualLockerQuotaStatus.CREATED -> {
                when (next) {
                    ManualLockerQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualLockerQuotaStatus.VALIDATED -> {
                when (next) {
                    ManualLockerQuotaStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualLockerQuotaStatus.PUBLISHED -> return next == ManualLockerQuotaStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualLockerQuotaStatus) {
        if (!canMoveTo(next)) {
            error("manual locker quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((durationMinutes.toDouble() - minimum) * 100) / 100

    fun snapshot(): ManualLockerQuota {
        return ManualLockerQuota(
            code = code,
            label = label,
            driverId = driverId,
            capacity = capacity,
            depot = depot,
            durationMinutes = durationMinutes,
            minimum = minimum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual locker quota in status created. */
fun newManualLockerQuota(code: String, label: String): ManualLockerQuota {
    return ManualLockerQuota(
        code = code,
        label = label,
        driverId = "",
        capacity = 0,
        depot = "",
        durationMinutes = 0,
        minimum = 0.0,
        budget = 0.0,
        status = ManualLockerQuotaStatus.CREATED,
    )
}
