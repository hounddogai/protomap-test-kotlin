package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual defect target is in its life cycle. */
enum class ManualDefectTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual defect target. Its code is unique within a tenant. */
data class ManualDefectTarget(
    var code: String,
    var label: String,
    var ownerId: String,
    var quantity: Int,
    var score: Double,
    var active: Boolean,
    var status: ManualDefectTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDefectTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDefectTargetStatus): Boolean {
        when (status) {
            ManualDefectTargetStatus.CREATED -> {
                when (next) {
                    ManualDefectTargetStatus.VALIDATED -> return true
                }
                return false
            }
            ManualDefectTargetStatus.VALIDATED -> return next == ManualDefectTargetStatus.RETIRED
            ManualDefectTargetStatus.PUBLISHED -> {
                when (next) {
                    ManualDefectTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualDefectTargetStatus) {
        if (!canMoveTo(next)) {
            error("manual defect target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveTotal(): Double = round((score + quantity.toDouble()) * 100) / 100

    fun snapshot(): ManualDefectTarget {
        return ManualDefectTarget(
            code = code,
            label = label,
            ownerId = ownerId,
            quantity = quantity,
            score = score,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual defect target in status created. */
fun newManualDefectTarget(code: String, label: String): ManualDefectTarget {
    return ManualDefectTarget(
        code = code,
        label = label,
        ownerId = "",
        quantity = 0,
        score = 0.0,
        active = false,
        status = ManualDefectTargetStatus.CREATED,
    )
}
