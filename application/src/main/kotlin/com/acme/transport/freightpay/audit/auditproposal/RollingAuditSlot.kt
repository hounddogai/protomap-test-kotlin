package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling audit slot is in its life cycle. */
enum class RollingAuditSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling audit slot as the audit team keeps it. */
data class RollingAuditSlot(
    var code: String,
    var name: String,
    var createdAt: Int,
    var weight: Double,
    var tags: MutableList<String>,
    var carrier: String,
    var version: Int,
    var status: RollingAuditSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAuditSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAuditSlotStatus): Boolean {
        when (status) {
            RollingAuditSlotStatus.NEW -> {
                when (next) {
                    RollingAuditSlotStatus.QUEUED, RollingAuditSlotStatus.DONE -> return true
                }
                return false
            }
            RollingAuditSlotStatus.QUEUED -> {
                when (next) {
                    RollingAuditSlotStatus.RUNNING, RollingAuditSlotStatus.DONE -> return true
                }
                return false
            }
            RollingAuditSlotStatus.RUNNING -> return next == RollingAuditSlotStatus.DONE
            RollingAuditSlotStatus.DONE -> return next == RollingAuditSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingAuditSlotStatus) {
        if (!canMoveTo(next)) {
            error("rolling audit slot $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedLoad(): Double = round((weight + version.toDouble()) * 100) / 100

    fun snapshot(): RollingAuditSlot {
        return RollingAuditSlot(
            code = code,
            name = name,
            createdAt = createdAt,
            weight = weight,
            tags = tags,
            carrier = carrier,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling audit slot in status new. */
fun newRollingAuditSlot(code: String, name: String): RollingAuditSlot {
    return RollingAuditSlot(
        code = code,
        name = name,
        createdAt = 0,
        weight = 0.0,
        tags = mutableListOf<String>(),
        carrier = "",
        version = 0,
        status = RollingAuditSlotStatus.NEW,
    )
}
