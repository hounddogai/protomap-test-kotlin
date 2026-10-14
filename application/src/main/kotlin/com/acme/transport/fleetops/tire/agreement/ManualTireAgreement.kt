package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual tire agreement is in its life cycle. */
enum class ManualTireAgreementStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual tire agreement, with what the planners need to act on it. */
data class ManualTireAgreement(
    var code: String,
    var label: String,
    var lane: String,
    var endsAt: Int,
    var cost: Double,
    var windowDays: Int,
    var amount: Double,
    var status: ManualTireAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTireAgreementStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTireAgreementStatus): Boolean {
        when (status) {
            ManualTireAgreementStatus.CREATED -> return next == ManualTireAgreementStatus.VALIDATED
            ManualTireAgreementStatus.VALIDATED -> {
                when (next) {
                    ManualTireAgreementStatus.PUBLISHED -> return true
                }
                return false
            }
            ManualTireAgreementStatus.PUBLISHED -> {
                when (next) {
                    ManualTireAgreementStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTireAgreementStatus) {
        if (!canMoveTo(next)) {
            error("manual tire agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round(windowDays.toDouble() * amount * 100) / 100

    fun snapshot(): ManualTireAgreement {
        return ManualTireAgreement(
            code = code,
            label = label,
            lane = lane,
            endsAt = endsAt,
            cost = cost,
            windowDays = windowDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual tire agreement in status created. */
fun newManualTireAgreement(code: String, label: String): ManualTireAgreement {
    return ManualTireAgreement(
        code = code,
        label = label,
        lane = "",
        endsAt = 0,
        cost = 0.0,
        windowDays = 0,
        amount = 0.0,
        status = ManualTireAgreementStatus.CREATED,
    )
}
