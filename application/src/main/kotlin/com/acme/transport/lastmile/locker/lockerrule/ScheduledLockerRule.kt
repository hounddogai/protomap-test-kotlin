package com.acme.transport.lastmile.locker.lockerrule

import com.acme.transport.kit.text.normalize

// The scheduled locker rule records of lastmile's locker work, and the rules, stores, and calculations around
// them.

/** Where a scheduled locker rule is in its life cycle. */
enum class ScheduledLockerRuleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One scheduled locker rule, with what the planners need to act on it. */
data class ScheduledLockerRule(
    var code: String,
    var label: String,
    var odometer: Int,
    var timezone: String,
    var contactEmail: String,
    var visible: Boolean,
    var notes: String,
    var vehicleId: String,
    var name: String,
    var status: ScheduledLockerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerRuleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerRuleStatus): Boolean {
        when (status) {
            ScheduledLockerRuleStatus.DRAFT -> return next == ScheduledLockerRuleStatus.PENDING || next == ScheduledLockerRuleStatus.CLOSED
            ScheduledLockerRuleStatus.PENDING -> {
                when (next) {
                    ScheduledLockerRuleStatus.ACTIVE -> return true
                }
                return false
            }
            ScheduledLockerRuleStatus.ACTIVE -> return next == ScheduledLockerRuleStatus.SUSPENDED || next == ScheduledLockerRuleStatus.CLOSED
            ScheduledLockerRuleStatus.SUSPENDED -> {
                when (next) {
                    ScheduledLockerRuleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLockerRuleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): ScheduledLockerRule {
        return ScheduledLockerRule(
            code = code,
            label = label,
            odometer = odometer,
            timezone = timezone,
            contactEmail = contactEmail,
            visible = visible,
            notes = notes,
            vehicleId = vehicleId,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled locker rule in status draft. */
fun newScheduledLockerRule(code: String, label: String): ScheduledLockerRule {
    return ScheduledLockerRule(
        code = code,
        label = label,
        odometer = 0,
        timezone = "",
        contactEmail = "",
        visible = false,
        notes = "",
        vehicleId = "",
        name = "",
        status = ScheduledLockerRuleStatus.DRAFT,
    )
}
