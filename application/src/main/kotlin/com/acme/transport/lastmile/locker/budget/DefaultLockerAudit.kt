package com.acme.transport.lastmile.locker.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default locker audit is in its life cycle. */
enum class DefaultLockerAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default locker audit, with what the planners need to act on it. */
data class DefaultLockerAudit(
    var code: String,
    var name: String,
    var discount: Double,
    var segment: String,
    var contactEmail: String,
    var endsAt: Int,
    var tags: MutableList<String>,
    var visible: Boolean,
    var approved: Boolean,
    var driverId: String,
    var country: String,
    var status: DefaultLockerAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultLockerAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultLockerAuditStatus): Boolean {
        when (status) {
            DefaultLockerAuditStatus.OPEN -> return next == DefaultLockerAuditStatus.REOPENED || next == DefaultLockerAuditStatus.ARCHIVED
            DefaultLockerAuditStatus.IN_REVIEW -> return next == DefaultLockerAuditStatus.RESOLVED || next == DefaultLockerAuditStatus.REOPENED
            DefaultLockerAuditStatus.RESOLVED -> {
                when (next) {
                    DefaultLockerAuditStatus.REOPENED, DefaultLockerAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultLockerAuditStatus.REOPENED -> return next == DefaultLockerAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultLockerAuditStatus) {
        if (!canMoveTo(next)) {
            error("default locker audit $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"default locker audit"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((endsAt.toDouble() + discount) * 100) / 100

    fun snapshot(): DefaultLockerAudit {
        return DefaultLockerAudit(
            code = code,
            name = name,
            discount = discount,
            segment = segment,
            contactEmail = contactEmail,
            endsAt = endsAt,
            tags = tags,
            visible = visible,
            approved = approved,
            driverId = driverId,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a default locker audit in status open. */
fun newDefaultLockerAudit(code: String, name: String): DefaultLockerAudit {
    return DefaultLockerAudit(
        code = code,
        name = name,
        discount = 0.0,
        segment = "",
        contactEmail = "",
        endsAt = 0,
        tags = mutableListOf<String>(),
        visible = false,
        approved = false,
        driverId = "",
        country = "",
        status = DefaultLockerAuditStatus.OPEN,
    )
}
