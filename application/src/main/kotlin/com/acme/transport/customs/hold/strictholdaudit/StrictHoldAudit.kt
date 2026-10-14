package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.kit.text.normalize

// The strict hold audit records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a strict hold audit is in its life cycle. */
enum class StrictHoldAuditStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict hold audit. Its code is unique within a tenant. */
data class StrictHoldAudit(
    var code: String,
    var label: String,
    var region: String,
    var revision: Int,
    var notes: String,
    var status: StrictHoldAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictHoldAuditStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictHoldAuditStatus): Boolean {
        when (status) {
            StrictHoldAuditStatus.CREATED -> return next == StrictHoldAuditStatus.PUBLISHED || next == StrictHoldAuditStatus.RETIRED
            StrictHoldAuditStatus.VALIDATED -> return next == StrictHoldAuditStatus.PUBLISHED
            StrictHoldAuditStatus.PUBLISHED -> return next == StrictHoldAuditStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictHoldAuditStatus) {
        if (!canMoveTo(next)) {
            error("strict hold audit $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): StrictHoldAudit {
        return StrictHoldAudit(
            code = code,
            label = label,
            region = region,
            revision = revision,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a strict hold audit in status created. */
fun newStrictHoldAudit(code: String, label: String): StrictHoldAudit {
    return StrictHoldAudit(
        code = code,
        label = label,
        region = "",
        revision = 0,
        notes = "",
        status = StrictHoldAuditStatus.CREATED,
    )
}
