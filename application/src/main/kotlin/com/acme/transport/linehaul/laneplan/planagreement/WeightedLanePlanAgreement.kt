package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.kit.text.normalize

// The weighted lane plan agreement records of linehaul's lane plan work, and the rules, stores, and
// calculations around them.

/** Where a weighted lane plan agreement is in its life cycle. */
enum class WeightedLanePlanAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted lane plan agreement as the lane plan team keeps it. */
data class WeightedLanePlanAgreement(
    var code: String,
    var label: String,
    var externalRef: String,
    var phone: String,
    var floorValue: Double,
    var status: WeightedLanePlanAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLanePlanAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLanePlanAgreementStatus): Boolean {
        when (status) {
            WeightedLanePlanAgreementStatus.PLANNED -> return next == WeightedLanePlanAgreementStatus.IN_PROGRESS || next == WeightedLanePlanAgreementStatus.CANCELLED
            WeightedLanePlanAgreementStatus.SCHEDULED -> {
                when (next) {
                    WeightedLanePlanAgreementStatus.IN_PROGRESS -> return true
                }
                return false
            }
            WeightedLanePlanAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    WeightedLanePlanAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedLanePlanAgreementStatus.COMPLETED -> {
                when (next) {
                    WeightedLanePlanAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedLanePlanAgreementStatus) {
        if (!canMoveTo(next)) {
            error("weighted lane plan agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = floorValue

    fun snapshot(): WeightedLanePlanAgreement {
        return WeightedLanePlanAgreement(
            code = code,
            label = label,
            externalRef = externalRef,
            phone = phone,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted lane plan agreement in status planned. */
fun newWeightedLanePlanAgreement(code: String, label: String): WeightedLanePlanAgreement {
    return WeightedLanePlanAgreement(
        code = code,
        label = label,
        externalRef = "",
        phone = "",
        floorValue = 0.0,
        status = WeightedLanePlanAgreementStatus.PLANNED,
    )
}
