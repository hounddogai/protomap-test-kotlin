package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.text.normalize

/** Where a fallback declaration record is in its life cycle. */
enum class FallbackDeclarationRecordStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback declaration record, with what the planners need to act on it. */
data class FallbackDeclarationRecord(
    var code: String,
    var label: String,
    var category: String,
    var notes: String,
    var discount: Double,
    var status: FallbackDeclarationRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDeclarationRecordStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDeclarationRecordStatus): Boolean {
        when (status) {
            FallbackDeclarationRecordStatus.OPEN -> {
                when (next) {
                    FallbackDeclarationRecordStatus.RESOLVED, FallbackDeclarationRecordStatus.REOPENED -> return true
                }
                return false
            }
            FallbackDeclarationRecordStatus.IN_REVIEW -> return next == FallbackDeclarationRecordStatus.RESOLVED
            FallbackDeclarationRecordStatus.RESOLVED -> {
                when (next) {
                    FallbackDeclarationRecordStatus.REOPENED -> return true
                }
                return false
            }
            FallbackDeclarationRecordStatus.REOPENED -> {
                when (next) {
                    FallbackDeclarationRecordStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDeclarationRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback declaration record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = discount

    fun snapshot(): FallbackDeclarationRecord {
        return FallbackDeclarationRecord(
            code = code,
            label = label,
            category = category,
            notes = notes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback declaration record in status open. */
fun newFallbackDeclarationRecord(code: String, label: String): FallbackDeclarationRecord {
    return FallbackDeclarationRecord(
        code = code,
        label = label,
        category = "",
        notes = "",
        discount = 0.0,
        status = FallbackDeclarationRecordStatus.OPEN,
    )
}
