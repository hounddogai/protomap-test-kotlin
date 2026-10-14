package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped duty period policy records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a capped duty period policy is in its life cycle. */
enum class CappedDutyPeriodPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One capped duty period policy, with what the planners need to act on it. */
data class CappedDutyPeriodPolicy(
    var code: String,
    var label: String,
    var priority: Int,
    var odometer: Int,
    var contactEmail: String,
    var endsAt: Int,
    var attempts: Int,
    var pallets: Int,
    var channel: String,
    var checksum: String,
    var loadKg: Double,
    var status: CappedDutyPeriodPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDutyPeriodPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDutyPeriodPolicyStatus): Boolean {
        when (status) {
            CappedDutyPeriodPolicyStatus.IDLE -> {
                when (next) {
                    CappedDutyPeriodPolicyStatus.DRAINING, CappedDutyPeriodPolicyStatus.STOPPED -> return true
                }
                return false
            }
            CappedDutyPeriodPolicyStatus.WARMING -> {
                when (next) {
                    CappedDutyPeriodPolicyStatus.READY -> return true
                }
                return false
            }
            CappedDutyPeriodPolicyStatus.READY -> {
                when (next) {
                    CappedDutyPeriodPolicyStatus.STOPPED -> return true
                }
                return false
            }
            CappedDutyPeriodPolicyStatus.DRAINING -> {
                when (next) {
                    CappedDutyPeriodPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDutyPeriodPolicyStatus) {
        if (!canMoveTo(next)) {
            error("capped duty period policy $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"capped duty period policy"} $code"

    fun projectedValue(): Double = round(loadKg * priority.toDouble() * 100) / 100

    fun snapshot(): CappedDutyPeriodPolicy {
        return CappedDutyPeriodPolicy(
            code = code,
            label = label,
            priority = priority,
            odometer = odometer,
            contactEmail = contactEmail,
            endsAt = endsAt,
            attempts = attempts,
            pallets = pallets,
            channel = channel,
            checksum = checksum,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped duty period policy in status idle. */
fun newCappedDutyPeriodPolicy(code: String, label: String): CappedDutyPeriodPolicy {
    return CappedDutyPeriodPolicy(
        code = code,
        label = label,
        priority = 0,
        odometer = 0,
        contactEmail = "",
        endsAt = 0,
        attempts = 0,
        pallets = 0,
        channel = "",
        checksum = "",
        loadKg = 0.0,
        status = CappedDutyPeriodPolicyStatus.IDLE,
    )
}
