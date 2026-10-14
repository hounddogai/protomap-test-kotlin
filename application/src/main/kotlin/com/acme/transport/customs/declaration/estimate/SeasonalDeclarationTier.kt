package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal declaration tier is in its life cycle. */
enum class SeasonalDeclarationTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal declaration tier as the declaration team keeps it. */
data class SeasonalDeclarationTier(
    var code: String,
    var title: String,
    var revision: Int,
    var summary: String,
    var sequence: Int,
    var status: SeasonalDeclarationTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDeclarationTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDeclarationTierStatus): Boolean {
        when (status) {
            SeasonalDeclarationTierStatus.PLANNED -> {
                when (next) {
                    SeasonalDeclarationTierStatus.IN_PROGRESS, SeasonalDeclarationTierStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalDeclarationTierStatus.SCHEDULED -> return next == SeasonalDeclarationTierStatus.COMPLETED || next == SeasonalDeclarationTierStatus.CANCELLED
            SeasonalDeclarationTierStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalDeclarationTierStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalDeclarationTierStatus.COMPLETED -> return next == SeasonalDeclarationTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalDeclarationTierStatus) {
        if (!canMoveTo(next)) {
            error("seasonal declaration tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun netLoad(): Double = round(revision.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): SeasonalDeclarationTier {
        return SeasonalDeclarationTier(
            code = code,
            title = title,
            revision = revision,
            summary = summary,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal declaration tier in status planned. */
fun newSeasonalDeclarationTier(code: String, title: String): SeasonalDeclarationTier {
    return SeasonalDeclarationTier(
        code = code,
        title = title,
        revision = 0,
        summary = "",
        sequence = 0,
        status = SeasonalDeclarationTierStatus.PLANNED,
    )
}
