package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.text.normalize

// The manual defect agreement records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a manual defect agreement is in its life cycle. */
enum class ManualDefectAgreementStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual defect agreement. Its code is unique within a tenant. */
data class ManualDefectAgreement(
    var code: String,
    var title: String,
    var active: Boolean,
    var locale: String,
    var visible: Boolean,
    var currency: String,
    var channel: String,
    var timezone: String,
    var endsAt: Int,
    var reviewer: String,
    var status: ManualDefectAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDefectAgreementStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDefectAgreementStatus): Boolean {
        when (status) {
            ManualDefectAgreementStatus.PROPOSED -> return next == ManualDefectAgreementStatus.APPROVED
            ManualDefectAgreementStatus.APPROVED -> {
                when (next) {
                    ManualDefectAgreementStatus.REJECTED -> return true
                }
                return false
            }
            ManualDefectAgreementStatus.REJECTED -> {
                when (next) {
                    ManualDefectAgreementStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDefectAgreementStatus) {
        if (!canMoveTo(next)) {
            error("manual defect agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): ManualDefectAgreement {
        return ManualDefectAgreement(
            code = code,
            title = title,
            active = active,
            locale = locale,
            visible = visible,
            currency = currency,
            channel = channel,
            timezone = timezone,
            endsAt = endsAt,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual defect agreement in status proposed. */
fun newManualDefectAgreement(code: String, title: String): ManualDefectAgreement {
    return ManualDefectAgreement(
        code = code,
        title = title,
        active = false,
        locale = "",
        visible = false,
        currency = "",
        channel = "",
        timezone = "",
        endsAt = 0,
        reviewer = "",
        status = ManualDefectAgreementStatus.PROPOSED,
    )
}
