package com.acme.transport.fleetops.leasereturn.returnschedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary lease return schedule records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a primary lease return schedule is in its life cycle. */
enum class PrimaryLeaseReturnScheduleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary lease return schedule. Its code is unique within a tenant. */
data class PrimaryLeaseReturnSchedule(
    var code: String,
    var label: String,
    var source: String,
    var updatedAt: Int,
    var timezone: String,
    var currency: String,
    var revision: Int,
    var shiftCode: String,
    var status: PrimaryLeaseReturnScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLeaseReturnScheduleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLeaseReturnScheduleStatus): Boolean {
        when (status) {
            PrimaryLeaseReturnScheduleStatus.CREATED -> {
                when (next) {
                    PrimaryLeaseReturnScheduleStatus.VALIDATED -> return true
                }
                return false
            }
            PrimaryLeaseReturnScheduleStatus.VALIDATED -> {
                when (next) {
                    PrimaryLeaseReturnScheduleStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryLeaseReturnScheduleStatus.PUBLISHED -> {
                when (next) {
                    PrimaryLeaseReturnScheduleStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryLeaseReturnScheduleStatus) {
        if (!canMoveTo(next)) {
            error("primary lease return schedule $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round((updatedAt.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryLeaseReturnSchedule {
        return PrimaryLeaseReturnSchedule(
            code = code,
            label = label,
            source = source,
            updatedAt = updatedAt,
            timezone = timezone,
            currency = currency,
            revision = revision,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary lease return schedule in status created. */
fun newPrimaryLeaseReturnSchedule(code: String, label: String): PrimaryLeaseReturnSchedule {
    return PrimaryLeaseReturnSchedule(
        code = code,
        label = label,
        source = "",
        updatedAt = 0,
        timezone = "",
        currency = "",
        revision = 0,
        shiftCode = "",
        status = PrimaryLeaseReturnScheduleStatus.CREATED,
    )
}
