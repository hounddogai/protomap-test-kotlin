package com.acme.transport.terminal.dockdoor.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dock door draft is in its life cycle. */
enum class StrictDockDoorDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict dock door draft. Its code is unique within a tenant. */
data class StrictDockDoorDraft(
    var code: String,
    var label: String,
    var maximum: Double,
    var contactEmail: String,
    var shiftCode: String,
    var phone: String,
    var cost: Double,
    var createdAt: Int,
    var approved: Boolean,
    var status: StrictDockDoorDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorDraftStatus): Boolean {
        when (status) {
            StrictDockDoorDraftStatus.OPEN -> {
                when (next) {
                    StrictDockDoorDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDockDoorDraftStatus.IN_REVIEW -> {
                when (next) {
                    StrictDockDoorDraftStatus.REOPENED, StrictDockDoorDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDockDoorDraftStatus.RESOLVED -> {
                when (next) {
                    StrictDockDoorDraftStatus.REOPENED -> return true
                }
                return false
            }
            StrictDockDoorDraftStatus.REOPENED -> {
                when (next) {
                    StrictDockDoorDraftStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDockDoorDraftStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedTotal(): Double = round(maximum * createdAt.toDouble() * 100) / 100

    fun snapshot(): StrictDockDoorDraft {
        return StrictDockDoorDraft(
            code = code,
            label = label,
            maximum = maximum,
            contactEmail = contactEmail,
            shiftCode = shiftCode,
            phone = phone,
            cost = cost,
            createdAt = createdAt,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict dock door draft in status open. */
fun newStrictDockDoorDraft(code: String, label: String): StrictDockDoorDraft {
    return StrictDockDoorDraft(
        code = code,
        label = label,
        maximum = 0.0,
        contactEmail = "",
        shiftCode = "",
        phone = "",
        cost = 0.0,
        createdAt = 0,
        approved = false,
        status = StrictDockDoorDraftStatus.OPEN,
    )
}
