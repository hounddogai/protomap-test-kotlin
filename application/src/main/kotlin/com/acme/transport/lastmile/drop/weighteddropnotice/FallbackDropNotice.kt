package com.acme.transport.lastmile.drop.weighteddropnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback drop notice records of lastmile's drop work, and the rules, stores, and calculations around
// them.

/** Where a fallback drop notice is in its life cycle. */
enum class FallbackDropNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback drop notice as the drop team keeps it. */
data class FallbackDropNotice(
    var code: String,
    var name: String,
    var createdAt: Int,
    var floorValue: Double,
    var stopCount: Int,
    var revision: Int,
    var externalRef: String,
    var status: FallbackDropNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDropNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDropNoticeStatus): Boolean {
        when (status) {
            FallbackDropNoticeStatus.PROPOSED -> {
                when (next) {
                    FallbackDropNoticeStatus.APPROVED -> return true
                }
                return false
            }
            FallbackDropNoticeStatus.APPROVED -> {
                when (next) {
                    FallbackDropNoticeStatus.REJECTED -> return true
                }
                return false
            }
            FallbackDropNoticeStatus.REJECTED -> return next == FallbackDropNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackDropNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback drop notice $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun adjustedVolume(): Double = round((floorValue - createdAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackDropNotice {
        return FallbackDropNotice(
            code = code,
            name = name,
            createdAt = createdAt,
            floorValue = floorValue,
            stopCount = stopCount,
            revision = revision,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback drop notice in status proposed. */
fun newFallbackDropNotice(code: String, name: String): FallbackDropNotice {
    return FallbackDropNotice(
        code = code,
        name = name,
        createdAt = 0,
        floorValue = 0.0,
        stopCount = 0,
        revision = 0,
        externalRef = "",
        status = FallbackDropNoticeStatus.PROPOSED,
    )
}
