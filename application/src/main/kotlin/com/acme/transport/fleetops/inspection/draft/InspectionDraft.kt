package com.acme.transport.fleetops.inspection.draft

import com.acme.transport.kit.text.normalize

// The inspection draft records of fleetops's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection draft is in its life cycle. */
enum class InspectionDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a inspection draft and its current status. */
data class InspectionDraft(
    var code: String,
    var name: String,
    var archived: Boolean,
    var approved: Boolean,
    var loadKg: Double,
    var routeCode: String,
    var status: InspectionDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionDraftStatus): Boolean {
        when (status) {
            InspectionDraftStatus.DRAFT -> return next == InspectionDraftStatus.SUSPENDED || next == InspectionDraftStatus.CLOSED
            InspectionDraftStatus.PENDING -> {
                when (next) {
                    InspectionDraftStatus.CLOSED -> return true
                }
                return false
            }
            InspectionDraftStatus.ACTIVE -> return next == InspectionDraftStatus.SUSPENDED || next == InspectionDraftStatus.CLOSED
            InspectionDraftStatus.SUSPENDED -> {
                when (next) {
                    InspectionDraftStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionDraftStatus) {
        if (!canMoveTo(next)) {
            error("inspection draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection draft"} $code"

    fun measuredValue(): Double = loadKg

    fun snapshot(): InspectionDraft {
        return InspectionDraft(
            code = code,
            name = name,
            archived = archived,
            approved = approved,
            loadKg = loadKg,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection draft in status draft. */
fun newInspectionDraft(code: String, name: String): InspectionDraft {
    return InspectionDraft(
        code = code,
        name = name,
        archived = false,
        approved = false,
        loadKg = 0.0,
        routeCode = "",
        status = InspectionDraftStatus.DRAFT,
    )
}
