package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.normalize

// The manual lane plan target records of linehaul's lane plan work, and the rules, stores, and calculations
// around them.

/** Where a manual lane plan target is in its life cycle. */
enum class ManualLanePlanTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual lane plan target and its current status. */
data class ManualLanePlanTarget(
    var code: String,
    var label: String,
    var depot: String,
    var approved: Boolean,
    var tags: MutableList<String>,
    var contactEmail: String,
    var etaMinutes: Int,
    var summary: String,
    var checksum: String,
    var unit: String,
    var status: ManualLanePlanTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualLanePlanTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualLanePlanTargetStatus): Boolean {
        when (status) {
            ManualLanePlanTargetStatus.PLANNED -> return next == ManualLanePlanTargetStatus.IN_PROGRESS || next == ManualLanePlanTargetStatus.COMPLETED
            ManualLanePlanTargetStatus.SCHEDULED -> return next == ManualLanePlanTargetStatus.COMPLETED
            ManualLanePlanTargetStatus.IN_PROGRESS -> {
                when (next) {
                    ManualLanePlanTargetStatus.COMPLETED -> return true
                }
                return false
            }
            ManualLanePlanTargetStatus.COMPLETED -> {
                when (next) {
                    ManualLanePlanTargetStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualLanePlanTargetStatus) {
        if (!canMoveTo(next)) {
            error("manual lane plan target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): ManualLanePlanTarget {
        return ManualLanePlanTarget(
            code = code,
            label = label,
            depot = depot,
            approved = approved,
            tags = tags,
            contactEmail = contactEmail,
            etaMinutes = etaMinutes,
            summary = summary,
            checksum = checksum,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a manual lane plan target in status planned. */
fun newManualLanePlanTarget(code: String, label: String): ManualLanePlanTarget {
    return ManualLanePlanTarget(
        code = code,
        label = label,
        depot = "",
        approved = false,
        tags = mutableListOf<String>(),
        contactEmail = "",
        etaMinutes = 0,
        summary = "",
        checksum = "",
        unit = "",
        status = ManualLanePlanTargetStatus.PLANNED,
    )
}
