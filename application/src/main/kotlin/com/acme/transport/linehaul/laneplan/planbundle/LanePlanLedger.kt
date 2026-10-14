package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lane plan ledger is in its life cycle. */
enum class LanePlanLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lane plan ledger, with what the planners need to act on it. */
data class LanePlanLedger(
    var code: String,
    var name: String,
    var graceDays: Int,
    var pinned: Boolean,
    var sequence: Int,
    var source: String,
    var label: String,
    var status: LanePlanLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanLedgerStatus): Boolean {
        when (status) {
            LanePlanLedgerStatus.PLANNED -> {
                when (next) {
                    LanePlanLedgerStatus.IN_PROGRESS, LanePlanLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            LanePlanLedgerStatus.SCHEDULED -> {
                when (next) {
                    LanePlanLedgerStatus.IN_PROGRESS, LanePlanLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            LanePlanLedgerStatus.IN_PROGRESS -> return next == LanePlanLedgerStatus.COMPLETED
            LanePlanLedgerStatus.COMPLETED -> return next == LanePlanLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LanePlanLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lane plan ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun netLoad(): Double = round((graceDays.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): LanePlanLedger {
        return LanePlanLedger(
            code = code,
            name = name,
            graceDays = graceDays,
            pinned = pinned,
            sequence = sequence,
            source = source,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lane plan ledger in status planned. */
fun newLanePlanLedger(code: String, name: String): LanePlanLedger {
    return LanePlanLedger(
        code = code,
        name = name,
        graceDays = 0,
        pinned = false,
        sequence = 0,
        source = "",
        label = "",
        status = LanePlanLedgerStatus.PLANNED,
    )
}
