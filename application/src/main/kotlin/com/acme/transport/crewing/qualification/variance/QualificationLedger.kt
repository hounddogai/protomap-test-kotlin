package com.acme.transport.crewing.qualification.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a qualification ledger is in its life cycle. */
enum class QualificationLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a qualification ledger and its current status. */
data class QualificationLedger(
    var code: String,
    var name: String,
    var pallets: Int,
    var ratio: Double,
    var floorValue: Double,
    var channel: String,
    var status: QualificationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            QualificationLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: QualificationLedgerStatus): Boolean {
        when (status) {
            QualificationLedgerStatus.CREATED -> return next == QualificationLedgerStatus.PUBLISHED
            QualificationLedgerStatus.VALIDATED -> {
                when (next) {
                    QualificationLedgerStatus.RETIRED -> return true
                }
                return false
            }
            QualificationLedgerStatus.PUBLISHED -> {
                when (next) {
                    QualificationLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: QualificationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("qualification ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedLoad(): Double = round((pallets.toDouble() + floorValue) * 100) / 100

    fun snapshot(): QualificationLedger {
        return QualificationLedger(
            code = code,
            name = name,
            pallets = pallets,
            ratio = ratio,
            floorValue = floorValue,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a qualification ledger in status created. */
fun newQualificationLedger(code: String, name: String): QualificationLedger {
    return QualificationLedger(
        code = code,
        name = name,
        pallets = 0,
        ratio = 0.0,
        floorValue = 0.0,
        channel = "",
        status = QualificationLedgerStatus.CREATED,
    )
}
