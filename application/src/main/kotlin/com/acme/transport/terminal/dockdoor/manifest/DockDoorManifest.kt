package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dock door manifest records of terminal's dock door work, and the rules, stores, and calculations around
// them.

/** Where a dock door manifest is in its life cycle. */
enum class DockDoorManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A dock door manifest. Its code is unique within a tenant. */
data class DockDoorManifest(
    var code: String,
    var label: String,
    var endsAt: Int,
    var approved: Boolean,
    var budget: Double,
    var attempts: Int,
    var etaMinutes: Int,
    var status: DockDoorManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorManifestStatus): Boolean {
        when (status) {
            DockDoorManifestStatus.OPEN -> {
                when (next) {
                    DockDoorManifestStatus.IN_REVIEW -> return true
                }
                return false
            }
            DockDoorManifestStatus.IN_REVIEW -> {
                when (next) {
                    DockDoorManifestStatus.RESOLVED -> return true
                }
                return false
            }
            DockDoorManifestStatus.RESOLVED -> return next == DockDoorManifestStatus.REOPENED || next == DockDoorManifestStatus.ARCHIVED
            DockDoorManifestStatus.REOPENED -> return next == DockDoorManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DockDoorManifestStatus) {
        if (!canMoveTo(next)) {
            error("dock door manifest $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedVolume(): Double = round((etaMinutes.toDouble() + budget) * 100) / 100

    fun snapshot(): DockDoorManifest {
        return DockDoorManifest(
            code = code,
            label = label,
            endsAt = endsAt,
            approved = approved,
            budget = budget,
            attempts = attempts,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a dock door manifest in status open. */
fun newDockDoorManifest(code: String, label: String): DockDoorManifest {
    return DockDoorManifest(
        code = code,
        label = label,
        endsAt = 0,
        approved = false,
        budget = 0.0,
        attempts = 0,
        etaMinutes = 0,
        status = DockDoorManifestStatus.OPEN,
    )
}
