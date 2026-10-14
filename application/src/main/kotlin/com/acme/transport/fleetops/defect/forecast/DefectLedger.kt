package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect ledger is in its life cycle. */
enum class DefectLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A defect ledger as the defect team keeps it. */
data class DefectLedger(
    var code: String,
    var title: String,
    var odometer: Int,
    var contactEmail: String,
    var sequence: Int,
    var externalRef: String,
    var floorValue: Double,
    var archived: Boolean,
    var visible: Boolean,
    var dock: String,
    var status: DefectLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectLedgerStatus): Boolean {
        when (status) {
            DefectLedgerStatus.REQUESTED -> {
                when (next) {
                    DefectLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            DefectLedgerStatus.GRANTED -> {
                when (next) {
                    DefectLedgerStatus.REVOKED, DefectLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            DefectLedgerStatus.REVOKED -> return next == DefectLedgerStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefectLedgerStatus) {
        if (!canMoveTo(next)) {
            error("defect ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedValue(): Double = round((sequence.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): DefectLedger {
        return DefectLedger(
            code = code,
            title = title,
            odometer = odometer,
            contactEmail = contactEmail,
            sequence = sequence,
            externalRef = externalRef,
            floorValue = floorValue,
            archived = archived,
            visible = visible,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a defect ledger in status requested. */
fun newDefectLedger(code: String, title: String): DefectLedger {
    return DefectLedger(
        code = code,
        title = title,
        odometer = 0,
        contactEmail = "",
        sequence = 0,
        externalRef = "",
        floorValue = 0.0,
        archived = false,
        visible = false,
        dock = "",
        status = DefectLedgerStatus.REQUESTED,
    )
}
