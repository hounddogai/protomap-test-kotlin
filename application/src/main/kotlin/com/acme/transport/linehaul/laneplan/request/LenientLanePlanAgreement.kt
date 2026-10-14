package com.acme.transport.linehaul.laneplan.request

import com.acme.transport.kit.text.normalize

/** Where a lenient lane plan agreement is in its life cycle. */
enum class LenientLanePlanAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One lenient lane plan agreement, with what the planners need to act on it. */
data class LenientLanePlanAgreement(
    var code: String,
    var title: String,
    var visible: Boolean,
    var floorValue: Double,
    var dock: String,
    var vehicleId: String,
    var status: LenientLanePlanAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLanePlanAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLanePlanAgreementStatus): Boolean {
        when (status) {
            LenientLanePlanAgreementStatus.PLANNED -> return next == LenientLanePlanAgreementStatus.COMPLETED || next == LenientLanePlanAgreementStatus.CANCELLED
            LenientLanePlanAgreementStatus.SCHEDULED -> {
                when (next) {
                    LenientLanePlanAgreementStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LenientLanePlanAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    LenientLanePlanAgreementStatus.COMPLETED -> return true
                }
                return false
            }
            LenientLanePlanAgreementStatus.COMPLETED -> {
                when (next) {
                    LenientLanePlanAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLanePlanAgreementStatus) {
        if (!canMoveTo(next)) {
            error("lenient lane plan agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = floorValue

    fun snapshot(): LenientLanePlanAgreement {
        return LenientLanePlanAgreement(
            code = code,
            title = title,
            visible = visible,
            floorValue = floorValue,
            dock = dock,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient lane plan agreement in status planned. */
fun newLenientLanePlanAgreement(code: String, title: String): LenientLanePlanAgreement {
    return LenientLanePlanAgreement(
        code = code,
        title = title,
        visible = false,
        floorValue = 0.0,
        dock = "",
        vehicleId = "",
        status = LenientLanePlanAgreementStatus.PLANNED,
    )
}
