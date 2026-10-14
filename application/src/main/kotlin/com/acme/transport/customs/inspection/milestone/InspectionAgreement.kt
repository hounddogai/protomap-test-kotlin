package com.acme.transport.customs.inspection.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection agreement is in its life cycle. */
enum class InspectionAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a inspection agreement and its current status. */
data class InspectionAgreement(
    var code: String,
    var title: String,
    var startsAt: Int,
    var updatedAt: Int,
    var unit: String,
    var label: String,
    var status: InspectionAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionAgreementStatus): Boolean {
        when (status) {
            InspectionAgreementStatus.CREATED -> return next == InspectionAgreementStatus.RETIRED
            InspectionAgreementStatus.VALIDATED -> return next == InspectionAgreementStatus.PUBLISHED || next == InspectionAgreementStatus.RETIRED
            InspectionAgreementStatus.PUBLISHED -> {
                when (next) {
                    InspectionAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionAgreementStatus) {
        if (!canMoveTo(next)) {
            error("inspection agreement $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"inspection agreement"} $code"

    fun projectedVolume(): Double = round(updatedAt.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): InspectionAgreement {
        return InspectionAgreement(
            code = code,
            title = title,
            startsAt = startsAt,
            updatedAt = updatedAt,
            unit = unit,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a inspection agreement in status created. */
fun newInspectionAgreement(code: String, title: String): InspectionAgreement {
    return InspectionAgreement(
        code = code,
        title = title,
        startsAt = 0,
        updatedAt = 0,
        unit = "",
        label = "",
        status = InspectionAgreementStatus.CREATED,
    )
}
