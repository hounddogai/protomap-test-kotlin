package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy defect agreement is in its life cycle. */
enum class LegacyDefectAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy defect agreement. Its code is unique within a tenant. */
data class LegacyDefectAgreement(
    var code: String,
    var name: String,
    var currency: String,
    var margin: Double,
    var locale: String,
    var odometer: Int,
    var carrier: String,
    var category: String,
    var sequence: Int,
    var status: LegacyDefectAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyDefectAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyDefectAgreementStatus): Boolean {
        when (status) {
            LegacyDefectAgreementStatus.NEW -> return next == LegacyDefectAgreementStatus.FAILED
            LegacyDefectAgreementStatus.QUEUED -> {
                when (next) {
                    LegacyDefectAgreementStatus.RUNNING -> return true
                }
                return false
            }
            LegacyDefectAgreementStatus.RUNNING -> return next == LegacyDefectAgreementStatus.DONE || next == LegacyDefectAgreementStatus.FAILED
            LegacyDefectAgreementStatus.DONE -> return next == LegacyDefectAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LegacyDefectAgreementStatus) {
        if (!canMoveTo(next)) {
            error("legacy defect agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((margin + sequence.toDouble()) * 100) / 100

    fun snapshot(): LegacyDefectAgreement {
        return LegacyDefectAgreement(
            code = code,
            name = name,
            currency = currency,
            margin = margin,
            locale = locale,
            odometer = odometer,
            carrier = carrier,
            category = category,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy defect agreement in status new. */
fun newLegacyDefectAgreement(code: String, name: String): LegacyDefectAgreement {
    return LegacyDefectAgreement(
        code = code,
        name = name,
        currency = "",
        margin = 0.0,
        locale = "",
        odometer = 0,
        carrier = "",
        category = "",
        sequence = 0,
        status = LegacyDefectAgreementStatus.NEW,
    )
}
