package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.text.normalize

// The seasonal declaration batch records of customs's declaration work, and the rules, stores, and
// calculations around them.

/** Where a seasonal declaration batch is in its life cycle. */
enum class SeasonalDeclarationBatchStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal declaration batch and its current status. */
data class SeasonalDeclarationBatch(
    var code: String,
    var title: String,
    var maximum: Double,
    var visible: Boolean,
    var ownerId: String,
    var unit: String,
    var depot: String,
    var approved: Boolean,
    var status: SeasonalDeclarationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDeclarationBatchStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDeclarationBatchStatus): Boolean {
        when (status) {
            SeasonalDeclarationBatchStatus.OPEN -> {
                when (next) {
                    SeasonalDeclarationBatchStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalDeclarationBatchStatus.IN_REVIEW -> return next == SeasonalDeclarationBatchStatus.REOPENED || next == SeasonalDeclarationBatchStatus.ARCHIVED
            SeasonalDeclarationBatchStatus.RESOLVED -> {
                when (next) {
                    SeasonalDeclarationBatchStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalDeclarationBatchStatus.REOPENED -> return next == SeasonalDeclarationBatchStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalDeclarationBatchStatus) {
        if (!canMoveTo(next)) {
            error("seasonal declaration batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = maximum

    fun snapshot(): SeasonalDeclarationBatch {
        return SeasonalDeclarationBatch(
            code = code,
            title = title,
            maximum = maximum,
            visible = visible,
            ownerId = ownerId,
            unit = unit,
            depot = depot,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal declaration batch in status open. */
fun newSeasonalDeclarationBatch(code: String, title: String): SeasonalDeclarationBatch {
    return SeasonalDeclarationBatch(
        code = code,
        title = title,
        maximum = 0.0,
        visible = false,
        ownerId = "",
        unit = "",
        depot = "",
        approved = false,
        status = SeasonalDeclarationBatchStatus.OPEN,
    )
}
