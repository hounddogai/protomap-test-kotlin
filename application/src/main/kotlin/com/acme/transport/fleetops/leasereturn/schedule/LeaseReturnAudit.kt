package com.acme.transport.fleetops.leasereturn.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lease return audit is in its life cycle. */
enum class LeaseReturnAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One lease return audit, with what the planners need to act on it. */
data class LeaseReturnAudit(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var stopCount: Int,
    var trailerId: String,
    var shiftCode: String,
    var approved: Boolean,
    var revision: Int,
    var checksum: String,
    var maximum: Double,
    var minimum: Double,
    var status: LeaseReturnAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnAuditStatus): Boolean {
        when (status) {
            LeaseReturnAuditStatus.NEW -> {
                when (next) {
                    LeaseReturnAuditStatus.DONE -> return true
                }
                return false
            }
            LeaseReturnAuditStatus.QUEUED -> return next == LeaseReturnAuditStatus.RUNNING || next == LeaseReturnAuditStatus.FAILED
            LeaseReturnAuditStatus.RUNNING -> {
                when (next) {
                    LeaseReturnAuditStatus.DONE -> return true
                }
                return false
            }
            LeaseReturnAuditStatus.DONE -> return next == LeaseReturnAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LeaseReturnAuditStatus) {
        if (!canMoveTo(next)) {
            error("lease return audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"lease return audit"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round(stopCount.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): LeaseReturnAudit {
        return LeaseReturnAudit(
            code = code,
            label = label,
            tags = tags,
            stopCount = stopCount,
            trailerId = trailerId,
            shiftCode = shiftCode,
            approved = approved,
            revision = revision,
            checksum = checksum,
            maximum = maximum,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lease return audit in status new. */
fun newLeaseReturnAudit(code: String, label: String): LeaseReturnAudit {
    return LeaseReturnAudit(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        stopCount = 0,
        trailerId = "",
        shiftCode = "",
        approved = false,
        revision = 0,
        checksum = "",
        maximum = 0.0,
        minimum = 0.0,
        status = LeaseReturnAuditStatus.NEW,
    )
}
