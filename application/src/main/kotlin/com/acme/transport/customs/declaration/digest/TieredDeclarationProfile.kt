package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered declaration profile is in its life cycle. */
enum class TieredDeclarationProfileStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A tiered declaration profile as the declaration team keeps it. */
data class TieredDeclarationProfile(
    var code: String,
    var title: String,
    var threshold: Double,
    var amount: Double,
    var stopCount: Int,
    var priority: Int,
    var sequence: Int,
    var archived: Boolean,
    var score: Double,
    var endsAt: Int,
    var status: TieredDeclarationProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDeclarationProfileStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDeclarationProfileStatus): Boolean {
        when (status) {
            TieredDeclarationProfileStatus.IDLE -> {
                when (next) {
                    TieredDeclarationProfileStatus.WARMING -> return true
                }
                return false
            }
            TieredDeclarationProfileStatus.WARMING -> return next == TieredDeclarationProfileStatus.READY || next == TieredDeclarationProfileStatus.STOPPED
            TieredDeclarationProfileStatus.READY -> return next == TieredDeclarationProfileStatus.DRAINING || next == TieredDeclarationProfileStatus.STOPPED
            TieredDeclarationProfileStatus.DRAINING -> return next == TieredDeclarationProfileStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TieredDeclarationProfileStatus) {
        if (!canMoveTo(next)) {
            error("tiered declaration profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tiered declaration profile"} $code"

    fun projectedLoad(): Double = round(stopCount.toDouble() * threshold * 100) / 100

    fun snapshot(): TieredDeclarationProfile {
        return TieredDeclarationProfile(
            code = code,
            title = title,
            threshold = threshold,
            amount = amount,
            stopCount = stopCount,
            priority = priority,
            sequence = sequence,
            archived = archived,
            score = score,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered declaration profile in status idle. */
fun newTieredDeclarationProfile(code: String, title: String): TieredDeclarationProfile {
    return TieredDeclarationProfile(
        code = code,
        title = title,
        threshold = 0.0,
        amount = 0.0,
        stopCount = 0,
        priority = 0,
        sequence = 0,
        archived = false,
        score = 0.0,
        endsAt = 0,
        status = TieredDeclarationProfileStatus.IDLE,
    )
}
