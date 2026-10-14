package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.normalize

/** Where a duty period agreement is in its life cycle. */
enum class DutyPeriodAgreementStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A duty period agreement. Its code is unique within a tenant. */
data class DutyPeriodAgreement(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var visible: Boolean,
    var driverId: String,
    var capacity: Int,
    var status: DutyPeriodAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodAgreementStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodAgreementStatus): Boolean {
        when (status) {
            DutyPeriodAgreementStatus.NEW -> {
                when (next) {
                    DutyPeriodAgreementStatus.DONE -> return true
                }
                return false
            }
            DutyPeriodAgreementStatus.QUEUED -> return next == DutyPeriodAgreementStatus.DONE || next == DutyPeriodAgreementStatus.FAILED
            DutyPeriodAgreementStatus.RUNNING -> {
                when (next) {
                    DutyPeriodAgreementStatus.DONE, DutyPeriodAgreementStatus.FAILED -> return true
                }
                return false
            }
            DutyPeriodAgreementStatus.DONE -> return next == DutyPeriodAgreementStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DutyPeriodAgreementStatus) {
        if (!canMoveTo(next)) {
            error("duty period agreement $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"duty period agreement"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): DutyPeriodAgreement {
        return DutyPeriodAgreement(
            code = code,
            label = label,
            attributes = attributes,
            channel = channel,
            visible = visible,
            driverId = driverId,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a duty period agreement in status new. */
fun newDutyPeriodAgreement(code: String, label: String): DutyPeriodAgreement {
    return DutyPeriodAgreement(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        channel = "",
        visible = false,
        driverId = "",
        capacity = 0,
        status = DutyPeriodAgreementStatus.NEW,
    )
}
