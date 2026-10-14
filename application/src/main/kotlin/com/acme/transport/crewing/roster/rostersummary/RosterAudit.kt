package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.text.normalize

/** Where a roster audit is in its life cycle. */
enum class RosterAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A roster audit as the roster team keeps it. */
data class RosterAudit(
    var code: String,
    var label: String,
    var phone: String,
    var approved: Boolean,
    var segment: String,
    var odometer: Int,
    var status: RosterAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterAuditStatus): Boolean {
        when (status) {
            RosterAuditStatus.NEW -> {
                when (next) {
                    RosterAuditStatus.DONE, RosterAuditStatus.FAILED -> return true
                }
                return false
            }
            RosterAuditStatus.QUEUED -> return next == RosterAuditStatus.RUNNING || next == RosterAuditStatus.DONE
            RosterAuditStatus.RUNNING -> return next == RosterAuditStatus.DONE
            RosterAuditStatus.DONE -> {
                when (next) {
                    RosterAuditStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterAuditStatus) {
        if (!canMoveTo(next)) {
            error("roster audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster audit"} $code"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): RosterAudit {
        return RosterAudit(
            code = code,
            label = label,
            phone = phone,
            approved = approved,
            segment = segment,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a roster audit in status new. */
fun newRosterAudit(code: String, label: String): RosterAudit {
    return RosterAudit(
        code = code,
        label = label,
        phone = "",
        approved = false,
        segment = "",
        odometer = 0,
        status = RosterAuditStatus.NEW,
    )
}
