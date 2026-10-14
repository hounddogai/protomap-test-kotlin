package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection ledger is in its life cycle. */
enum class InspectionLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A inspection ledger. Its code is unique within a tenant. */
data class InspectionLedger(
    var code: String,
    var name: String,
    var reviewer: String,
    var notes: String,
    var country: String,
    var updatedAt: Int,
    var priority: Int,
    var description: String,
    var segment: String,
    var category: String,
    var floorValue: Double,
    var status: InspectionLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionLedgerStatus): Boolean {
        when (status) {
            InspectionLedgerStatus.CREATED -> return next == InspectionLedgerStatus.VALIDATED
            InspectionLedgerStatus.VALIDATED -> {
                when (next) {
                    InspectionLedgerStatus.PUBLISHED, InspectionLedgerStatus.RETIRED -> return true
                }
                return false
            }
            InspectionLedgerStatus.PUBLISHED -> {
                when (next) {
                    InspectionLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionLedgerStatus) {
        if (!canMoveTo(next)) {
            error("inspection ledger $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedVolume(): Double = round((floorValue + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): InspectionLedger {
        return InspectionLedger(
            code = code,
            name = name,
            reviewer = reviewer,
            notes = notes,
            country = country,
            updatedAt = updatedAt,
            priority = priority,
            description = description,
            segment = segment,
            category = category,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a inspection ledger in status created. */
fun newInspectionLedger(code: String, name: String): InspectionLedger {
    return InspectionLedger(
        code = code,
        name = name,
        reviewer = "",
        notes = "",
        country = "",
        updatedAt = 0,
        priority = 0,
        description = "",
        segment = "",
        category = "",
        floorValue = 0.0,
        status = InspectionLedgerStatus.CREATED,
    )
}
