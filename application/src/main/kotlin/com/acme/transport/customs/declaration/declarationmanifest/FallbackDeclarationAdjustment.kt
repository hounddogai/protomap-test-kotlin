package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback declaration adjustment is in its life cycle. */
enum class FallbackDeclarationAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback declaration adjustment as the declaration team keeps it. */
data class FallbackDeclarationAdjustment(
    var code: String,
    var name: String,
    var carrier: String,
    var score: Double,
    var ceiling: Double,
    var summary: String,
    var endsAt: Int,
    var ownerId: String,
    var shiftCode: String,
    var stopCount: Int,
    var status: FallbackDeclarationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDeclarationAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDeclarationAdjustmentStatus): Boolean {
        when (status) {
            FallbackDeclarationAdjustmentStatus.PROPOSED -> return next == FallbackDeclarationAdjustmentStatus.REJECTED
            FallbackDeclarationAdjustmentStatus.APPROVED -> return next == FallbackDeclarationAdjustmentStatus.REJECTED || next == FallbackDeclarationAdjustmentStatus.WITHDRAWN
            FallbackDeclarationAdjustmentStatus.REJECTED -> {
                when (next) {
                    FallbackDeclarationAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDeclarationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback declaration adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveValue(): Double = round((stopCount.toDouble() - score) * 100) / 100

    fun snapshot(): FallbackDeclarationAdjustment {
        return FallbackDeclarationAdjustment(
            code = code,
            name = name,
            carrier = carrier,
            score = score,
            ceiling = ceiling,
            summary = summary,
            endsAt = endsAt,
            ownerId = ownerId,
            shiftCode = shiftCode,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback declaration adjustment in status proposed. */
fun newFallbackDeclarationAdjustment(code: String, name: String): FallbackDeclarationAdjustment {
    return FallbackDeclarationAdjustment(
        code = code,
        name = name,
        carrier = "",
        score = 0.0,
        ceiling = 0.0,
        summary = "",
        endsAt = 0,
        ownerId = "",
        shiftCode = "",
        stopCount = 0,
        status = FallbackDeclarationAdjustmentStatus.PROPOSED,
    )
}
