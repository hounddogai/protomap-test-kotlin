package com.acme.transport.network.node.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback node notice is in its life cycle. */
enum class FallbackNodeNoticeStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback node notice, with what the planners need to act on it. */
data class FallbackNodeNotice(
    var code: String,
    var name: String,
    var score: Double,
    var fuelLiters: Double,
    var notes: String,
    var graceDays: Int,
    var description: String,
    var retries: Int,
    var durationMinutes: Int,
    var version: Int,
    var status: FallbackNodeNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackNodeNoticeStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackNodeNoticeStatus): Boolean {
        when (status) {
            FallbackNodeNoticeStatus.OPEN -> return next == FallbackNodeNoticeStatus.IN_REVIEW || next == FallbackNodeNoticeStatus.REOPENED
            FallbackNodeNoticeStatus.IN_REVIEW -> {
                when (next) {
                    FallbackNodeNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackNodeNoticeStatus.RESOLVED -> {
                when (next) {
                    FallbackNodeNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackNodeNoticeStatus.REOPENED -> {
                when (next) {
                    FallbackNodeNoticeStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackNodeNoticeStatus) {
        if (!canMoveTo(next)) {
            error("fallback node notice $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"fallback node notice"} $code"

    fun weightedLoad(): Double = round((version.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackNodeNotice {
        return FallbackNodeNotice(
            code = code,
            name = name,
            score = score,
            fuelLiters = fuelLiters,
            notes = notes,
            graceDays = graceDays,
            description = description,
            retries = retries,
            durationMinutes = durationMinutes,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback node notice in status open. */
fun newFallbackNodeNotice(code: String, name: String): FallbackNodeNotice {
    return FallbackNodeNotice(
        code = code,
        name = name,
        score = 0.0,
        fuelLiters = 0.0,
        notes = "",
        graceDays = 0,
        description = "",
        retries = 0,
        durationMinutes = 0,
        version = 0,
        status = FallbackNodeNoticeStatus.OPEN,
    )
}
