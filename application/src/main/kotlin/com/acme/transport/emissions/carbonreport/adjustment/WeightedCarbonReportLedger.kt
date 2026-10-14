package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted carbon report ledger is in its life cycle. */
enum class WeightedCarbonReportLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A weighted carbon report ledger as the carbon report team keeps it. */
data class WeightedCarbonReportLedger(
    var code: String,
    var name: String,
    var attempts: Int,
    var pallets: Int,
    var batchSize: Int,
    var pinned: Boolean,
    var notes: String,
    var active: Boolean,
    var floorValue: Double,
    var contactEmail: String,
    var reviewer: String,
    var status: WeightedCarbonReportLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCarbonReportLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCarbonReportLedgerStatus): Boolean {
        when (status) {
            WeightedCarbonReportLedgerStatus.NEW -> {
                when (next) {
                    WeightedCarbonReportLedgerStatus.DONE, WeightedCarbonReportLedgerStatus.FAILED -> return true
                }
                return false
            }
            WeightedCarbonReportLedgerStatus.QUEUED -> {
                when (next) {
                    WeightedCarbonReportLedgerStatus.RUNNING, WeightedCarbonReportLedgerStatus.FAILED -> return true
                }
                return false
            }
            WeightedCarbonReportLedgerStatus.RUNNING -> {
                when (next) {
                    WeightedCarbonReportLedgerStatus.DONE, WeightedCarbonReportLedgerStatus.FAILED -> return true
                }
                return false
            }
            WeightedCarbonReportLedgerStatus.DONE -> return next == WeightedCarbonReportLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: WeightedCarbonReportLedgerStatus) {
        if (!canMoveTo(next)) {
            error("weighted carbon report ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((floorValue + pallets.toDouble()) * 100) / 100

    fun snapshot(): WeightedCarbonReportLedger {
        return WeightedCarbonReportLedger(
            code = code,
            name = name,
            attempts = attempts,
            pallets = pallets,
            batchSize = batchSize,
            pinned = pinned,
            notes = notes,
            active = active,
            floorValue = floorValue,
            contactEmail = contactEmail,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted carbon report ledger in status new. */
fun newWeightedCarbonReportLedger(code: String, name: String): WeightedCarbonReportLedger {
    return WeightedCarbonReportLedger(
        code = code,
        name = name,
        attempts = 0,
        pallets = 0,
        batchSize = 0,
        pinned = false,
        notes = "",
        active = false,
        floorValue = 0.0,
        contactEmail = "",
        reviewer = "",
        status = WeightedCarbonReportLedgerStatus.NEW,
    )
}
