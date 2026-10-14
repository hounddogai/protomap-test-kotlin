package com.acme.transport.network.hub.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient hub baseline is in its life cycle. */
enum class LenientHubBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient hub baseline and its current status. */
data class LenientHubBaseline(
    var code: String,
    var name: String,
    var visible: Boolean,
    var loadKg: Double,
    var ownerId: String,
    var score: Double,
    var endsAt: Int,
    var status: LenientHubBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientHubBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientHubBaselineStatus): Boolean {
        when (status) {
            LenientHubBaselineStatus.DRAFT -> return next == LenientHubBaselineStatus.PENDING || next == LenientHubBaselineStatus.SUSPENDED
            LenientHubBaselineStatus.PENDING -> {
                when (next) {
                    LenientHubBaselineStatus.ACTIVE, LenientHubBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientHubBaselineStatus.ACTIVE -> return next == LenientHubBaselineStatus.SUSPENDED
            LenientHubBaselineStatus.SUSPENDED -> return next == LenientHubBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientHubBaselineStatus) {
        if (!canMoveTo(next)) {
            error("lenient hub baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lenient hub baseline"} $code"

    fun adjustedVolume(): Double = round((score - loadKg) * 100) / 100

    fun snapshot(): LenientHubBaseline {
        return LenientHubBaseline(
            code = code,
            name = name,
            visible = visible,
            loadKg = loadKg,
            ownerId = ownerId,
            score = score,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient hub baseline in status draft. */
fun newLenientHubBaseline(code: String, name: String): LenientHubBaseline {
    return LenientHubBaseline(
        code = code,
        name = name,
        visible = false,
        loadKg = 0.0,
        ownerId = "",
        score = 0.0,
        endsAt = 0,
        status = LenientHubBaselineStatus.DRAFT,
    )
}
