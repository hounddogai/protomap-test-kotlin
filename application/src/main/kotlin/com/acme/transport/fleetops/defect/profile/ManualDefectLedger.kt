package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual defect ledger is in its life cycle. */
enum class ManualDefectLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One manual defect ledger, with what the planners need to act on it. */
data class ManualDefectLedger(
    var code: String,
    var label: String,
    var startsAt: Int,
    var reviewer: String,
    var region: String,
    var revision: Int,
    var visible: Boolean,
    var updatedAt: Int,
    var routeCode: String,
    var windowDays: Int,
    var status: ManualDefectLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDefectLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDefectLedgerStatus): Boolean {
        when (status) {
            ManualDefectLedgerStatus.OPEN -> return next == ManualDefectLedgerStatus.RESOLVED
            ManualDefectLedgerStatus.IN_REVIEW -> return next == ManualDefectLedgerStatus.RESOLVED || next == ManualDefectLedgerStatus.REOPENED
            ManualDefectLedgerStatus.RESOLVED -> {
                when (next) {
                    ManualDefectLedgerStatus.REOPENED -> return true
                }
                return false
            }
            ManualDefectLedgerStatus.REOPENED -> {
                when (next) {
                    ManualDefectLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDefectLedgerStatus) {
        if (!canMoveTo(next)) {
            error("manual defect ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"manual defect ledger"} $code"

    fun projectedExposure(): Double = round((updatedAt.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): ManualDefectLedger {
        return ManualDefectLedger(
            code = code,
            label = label,
            startsAt = startsAt,
            reviewer = reviewer,
            region = region,
            revision = revision,
            visible = visible,
            updatedAt = updatedAt,
            routeCode = routeCode,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual defect ledger in status open. */
fun newManualDefectLedger(code: String, label: String): ManualDefectLedger {
    return ManualDefectLedger(
        code = code,
        label = label,
        startsAt = 0,
        reviewer = "",
        region = "",
        revision = 0,
        visible = false,
        updatedAt = 0,
        routeCode = "",
        windowDays = 0,
        status = ManualDefectLedgerStatus.OPEN,
    )
}
