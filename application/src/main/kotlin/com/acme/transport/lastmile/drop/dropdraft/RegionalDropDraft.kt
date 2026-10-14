package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional drop draft records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a regional drop draft is in its life cycle. */
enum class RegionalDropDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional drop draft as the drop team keeps it. */
data class RegionalDropDraft(
    var code: String,
    var label: String,
    var batchSize: Int,
    var active: Boolean,
    var floorValue: Double,
    var checksum: String,
    var cost: Double,
    var sequence: Int,
    var status: RegionalDropDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDropDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDropDraftStatus): Boolean {
        when (status) {
            RegionalDropDraftStatus.PLANNED -> return next == RegionalDropDraftStatus.SCHEDULED || next == RegionalDropDraftStatus.IN_PROGRESS
            RegionalDropDraftStatus.SCHEDULED -> return next == RegionalDropDraftStatus.CANCELLED
            RegionalDropDraftStatus.IN_PROGRESS -> return next == RegionalDropDraftStatus.COMPLETED
            RegionalDropDraftStatus.COMPLETED -> return next == RegionalDropDraftStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RegionalDropDraftStatus) {
        if (!canMoveTo(next)) {
            error("regional drop draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedValue(): Double = round(batchSize.toDouble() * floorValue * 100) / 100

    fun snapshot(): RegionalDropDraft {
        return RegionalDropDraft(
            code = code,
            label = label,
            batchSize = batchSize,
            active = active,
            floorValue = floorValue,
            checksum = checksum,
            cost = cost,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional drop draft in status planned. */
fun newRegionalDropDraft(code: String, label: String): RegionalDropDraft {
    return RegionalDropDraft(
        code = code,
        label = label,
        batchSize = 0,
        active = false,
        floorValue = 0.0,
        checksum = "",
        cost = 0.0,
        sequence = 0,
        status = RegionalDropDraftStatus.PLANNED,
    )
}
