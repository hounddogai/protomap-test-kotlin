package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual coaching ledger is in its life cycle. */
enum class ManualCoachingLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual coaching ledger as the coaching team keeps it. */
data class ManualCoachingLedger(
    var code: String,
    var title: String,
    var version: Int,
    var budget: Double,
    var durationMinutes: Int,
    var dock: String,
    var description: String,
    var timezone: String,
    var segment: String,
    var active: Boolean,
    var distanceKm: Double,
    var status: ManualCoachingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCoachingLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCoachingLedgerStatus): Boolean {
        when (status) {
            ManualCoachingLedgerStatus.PLANNED -> return next == ManualCoachingLedgerStatus.COMPLETED
            ManualCoachingLedgerStatus.SCHEDULED -> return next == ManualCoachingLedgerStatus.COMPLETED
            ManualCoachingLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    ManualCoachingLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            ManualCoachingLedgerStatus.COMPLETED -> return next == ManualCoachingLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualCoachingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("manual coaching ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"manual coaching ledger"} $code"

    fun projectedValue(): Double = round(version.toDouble() * budget * 100) / 100

    fun snapshot(): ManualCoachingLedger {
        return ManualCoachingLedger(
            code = code,
            title = title,
            version = version,
            budget = budget,
            durationMinutes = durationMinutes,
            dock = dock,
            description = description,
            timezone = timezone,
            segment = segment,
            active = active,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a manual coaching ledger in status planned. */
fun newManualCoachingLedger(code: String, title: String): ManualCoachingLedger {
    return ManualCoachingLedger(
        code = code,
        title = title,
        version = 0,
        budget = 0.0,
        durationMinutes = 0,
        dock = "",
        description = "",
        timezone = "",
        segment = "",
        active = false,
        distanceKm = 0.0,
        status = ManualCoachingLedgerStatus.PLANNED,
    )
}
