package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual audit override is in its life cycle. */
enum class ManualAuditOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual audit override, with what the planners need to act on it. */
data class ManualAuditOverride(
    var code: String,
    var name: String,
    var minimum: Double,
    var sequence: Int,
    var visible: Boolean,
    var startsAt: Int,
    var externalRef: String,
    var graceDays: Int,
    var margin: Double,
    var status: ManualAuditOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAuditOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAuditOverrideStatus): Boolean {
        when (status) {
            ManualAuditOverrideStatus.DRAFT -> {
                when (next) {
                    ManualAuditOverrideStatus.SUSPENDED, ManualAuditOverrideStatus.CLOSED -> return true
                }
                return false
            }
            ManualAuditOverrideStatus.PENDING -> return next == ManualAuditOverrideStatus.SUSPENDED || next == ManualAuditOverrideStatus.CLOSED
            ManualAuditOverrideStatus.ACTIVE -> {
                when (next) {
                    ManualAuditOverrideStatus.SUSPENDED, ManualAuditOverrideStatus.CLOSED -> return true
                }
                return false
            }
            ManualAuditOverrideStatus.SUSPENDED -> {
                when (next) {
                    ManualAuditOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAuditOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual audit override $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"manual audit override"} $code"

    fun weightedVolume(): Double = round((margin - minimum) * 100) / 100

    fun snapshot(): ManualAuditOverride {
        return ManualAuditOverride(
            code = code,
            name = name,
            minimum = minimum,
            sequence = sequence,
            visible = visible,
            startsAt = startsAt,
            externalRef = externalRef,
            graceDays = graceDays,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual audit override in status draft. */
fun newManualAuditOverride(code: String, name: String): ManualAuditOverride {
    return ManualAuditOverride(
        code = code,
        name = name,
        minimum = 0.0,
        sequence = 0,
        visible = false,
        startsAt = 0,
        externalRef = "",
        graceDays = 0,
        margin = 0.0,
        status = ManualAuditOverrideStatus.DRAFT,
    )
}
