package com.acme.transport.crewing.qualification.qualificationpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual qualification target is in its life cycle. */
enum class ManualQualificationTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A manual qualification target as the qualification team keeps it. */
data class ManualQualificationTarget(
    var code: String,
    var label: String,
    var margin: Double,
    var etaMinutes: Int,
    var ceiling: Double,
    var currency: String,
    var minimum: Double,
    var priority: Int,
    var ownerId: String,
    var status: ManualQualificationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualQualificationTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualQualificationTargetStatus): Boolean {
        when (status) {
            ManualQualificationTargetStatus.PROPOSED -> {
                when (next) {
                    ManualQualificationTargetStatus.REJECTED, ManualQualificationTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualQualificationTargetStatus.APPROVED -> {
                when (next) {
                    ManualQualificationTargetStatus.REJECTED, ManualQualificationTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            ManualQualificationTargetStatus.REJECTED -> {
                when (next) {
                    ManualQualificationTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualQualificationTargetStatus) {
        if (!canMoveTo(next)) {
            error("manual qualification target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual qualification target"} $code"

    fun effectiveTotal(): Double = round(priority.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ManualQualificationTarget {
        return ManualQualificationTarget(
            code = code,
            label = label,
            margin = margin,
            etaMinutes = etaMinutes,
            ceiling = ceiling,
            currency = currency,
            minimum = minimum,
            priority = priority,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual qualification target in status proposed. */
fun newManualQualificationTarget(code: String, label: String): ManualQualificationTarget {
    return ManualQualificationTarget(
        code = code,
        label = label,
        margin = 0.0,
        etaMinutes = 0,
        ceiling = 0.0,
        currency = "",
        minimum = 0.0,
        priority = 0,
        ownerId = "",
        status = ManualQualificationTargetStatus.PROPOSED,
    )
}
