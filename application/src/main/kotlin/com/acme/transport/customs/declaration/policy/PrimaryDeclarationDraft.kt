package com.acme.transport.customs.declaration.policy

import com.acme.transport.kit.text.normalize

/** Where a primary declaration draft is in its life cycle. */
enum class PrimaryDeclarationDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One primary declaration draft, with what the planners need to act on it. */
data class PrimaryDeclarationDraft(
    var code: String,
    var label: String,
    var externalRef: String,
    var name: String,
    var etaMinutes: Int,
    var reviewer: String,
    var status: PrimaryDeclarationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryDeclarationDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryDeclarationDraftStatus): Boolean {
        when (status) {
            PrimaryDeclarationDraftStatus.IDLE -> return next == PrimaryDeclarationDraftStatus.READY || next == PrimaryDeclarationDraftStatus.STOPPED
            PrimaryDeclarationDraftStatus.WARMING -> return next == PrimaryDeclarationDraftStatus.READY
            PrimaryDeclarationDraftStatus.READY -> return next == PrimaryDeclarationDraftStatus.DRAINING || next == PrimaryDeclarationDraftStatus.STOPPED
            PrimaryDeclarationDraftStatus.DRAINING -> {
                when (next) {
                    PrimaryDeclarationDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryDeclarationDraftStatus) {
        if (!canMoveTo(next)) {
            error("primary declaration draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): PrimaryDeclarationDraft {
        return PrimaryDeclarationDraft(
            code = code,
            label = label,
            externalRef = externalRef,
            name = name,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary declaration draft in status idle. */
fun newPrimaryDeclarationDraft(code: String, label: String): PrimaryDeclarationDraft {
    return PrimaryDeclarationDraft(
        code = code,
        label = label,
        externalRef = "",
        name = "",
        etaMinutes = 0,
        reviewer = "",
        status = PrimaryDeclarationDraftStatus.IDLE,
    )
}
