package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual duty period ledger is in its life cycle. */
enum class ManualDutyPeriodLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual duty period ledger. Its code is unique within a tenant. */
data class ManualDutyPeriodLedger(
    var code: String,
    var label: String,
    var archived: Boolean,
    var capacity: Int,
    var notes: String,
    var lane: String,
    var limit: Int,
    var maximum: Double,
    var startsAt: Int,
    var checksum: String,
    var phone: String,
    var status: ManualDutyPeriodLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDutyPeriodLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDutyPeriodLedgerStatus): Boolean {
        when (status) {
            ManualDutyPeriodLedgerStatus.NEW -> {
                when (next) {
                    ManualDutyPeriodLedgerStatus.RUNNING -> return true
                }
                return false
            }
            ManualDutyPeriodLedgerStatus.QUEUED -> return next == ManualDutyPeriodLedgerStatus.FAILED
            ManualDutyPeriodLedgerStatus.RUNNING -> return next == ManualDutyPeriodLedgerStatus.DONE || next == ManualDutyPeriodLedgerStatus.FAILED
            ManualDutyPeriodLedgerStatus.DONE -> return next == ManualDutyPeriodLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualDutyPeriodLedgerStatus) {
        if (!canMoveTo(next)) {
            error("manual duty period ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual duty period ledger"} $code"

    fun effectiveVolume(): Double = round((limit.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): ManualDutyPeriodLedger {
        return ManualDutyPeriodLedger(
            code = code,
            label = label,
            archived = archived,
            capacity = capacity,
            notes = notes,
            lane = lane,
            limit = limit,
            maximum = maximum,
            startsAt = startsAt,
            checksum = checksum,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual duty period ledger in status new. */
fun newManualDutyPeriodLedger(code: String, label: String): ManualDutyPeriodLedger {
    return ManualDutyPeriodLedger(
        code = code,
        label = label,
        archived = false,
        capacity = 0,
        notes = "",
        lane = "",
        limit = 0,
        maximum = 0.0,
        startsAt = 0,
        checksum = "",
        phone = "",
        status = ManualDutyPeriodLedgerStatus.NEW,
    )
}
