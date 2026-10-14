package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal declaration target is in its life cycle. */
enum class SeasonalDeclarationTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One seasonal declaration target, with what the planners need to act on it. */
data class SeasonalDeclarationTarget(
    var code: String,
    var title: String,
    var externalRef: String,
    var expiresAt: Int,
    var description: String,
    var shiftCode: String,
    var maximum: Double,
    var status: SeasonalDeclarationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDeclarationTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDeclarationTargetStatus): Boolean {
        when (status) {
            SeasonalDeclarationTargetStatus.PROPOSED -> return next == SeasonalDeclarationTargetStatus.REJECTED || next == SeasonalDeclarationTargetStatus.WITHDRAWN
            SeasonalDeclarationTargetStatus.APPROVED -> {
                when (next) {
                    SeasonalDeclarationTargetStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalDeclarationTargetStatus.REJECTED -> {
                when (next) {
                    SeasonalDeclarationTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalDeclarationTargetStatus) {
        if (!canMoveTo(next)) {
            error("seasonal declaration target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round(expiresAt.toDouble() * maximum * 100) / 100

    fun snapshot(): SeasonalDeclarationTarget {
        return SeasonalDeclarationTarget(
            code = code,
            title = title,
            externalRef = externalRef,
            expiresAt = expiresAt,
            description = description,
            shiftCode = shiftCode,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal declaration target in status proposed. */
fun newSeasonalDeclarationTarget(code: String, title: String): SeasonalDeclarationTarget {
    return SeasonalDeclarationTarget(
        code = code,
        title = title,
        externalRef = "",
        expiresAt = 0,
        description = "",
        shiftCode = "",
        maximum = 0.0,
        status = SeasonalDeclarationTargetStatus.PROPOSED,
    )
}
