package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay ledger is in its life cycle. */
enum class RelayLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A relay ledger. Its code is unique within a tenant. */
data class RelayLedger(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var rate: Double,
    var ceiling: Double,
    var approved: Boolean,
    var archived: Boolean,
    var distanceKm: Double,
    var pinned: Boolean,
    var visible: Boolean,
    var category: String,
    var status: RelayLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayLedgerStatus): Boolean {
        when (status) {
            RelayLedgerStatus.NEW -> return next == RelayLedgerStatus.QUEUED
            RelayLedgerStatus.QUEUED -> return next == RelayLedgerStatus.RUNNING
            RelayLedgerStatus.RUNNING -> return next == RelayLedgerStatus.DONE
            RelayLedgerStatus.DONE -> return next == RelayLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RelayLedgerStatus) {
        if (!canMoveTo(next)) {
            error("relay ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"relay ledger"} $code"

    fun adjustedTotal(): Double = round((rate - distanceKm) * 100) / 100

    fun snapshot(): RelayLedger {
        return RelayLedger(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            rate = rate,
            ceiling = ceiling,
            approved = approved,
            archived = archived,
            distanceKm = distanceKm,
            pinned = pinned,
            visible = visible,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a relay ledger in status new. */
fun newRelayLedger(code: String, label: String): RelayLedger {
    return RelayLedger(
        code = code,
        label = label,
        durationMinutes = 0,
        rate = 0.0,
        ceiling = 0.0,
        approved = false,
        archived = false,
        distanceKm = 0.0,
        pinned = false,
        visible = false,
        category = "",
        status = RelayLedgerStatus.NEW,
    )
}
