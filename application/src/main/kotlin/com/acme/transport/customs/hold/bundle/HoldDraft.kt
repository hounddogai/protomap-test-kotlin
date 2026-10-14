package com.acme.transport.customs.hold.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold draft is in its life cycle. */
enum class HoldDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One hold draft, with what the planners need to act on it. */
data class HoldDraft(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var sequence: Int,
    var timezone: String,
    var priority: Int,
    var externalRef: String,
    var windowDays: Int,
    var label: String,
    var active: Boolean,
    var maximum: Double,
    var status: HoldDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldDraftStatus): Boolean {
        when (status) {
            HoldDraftStatus.REQUESTED -> {
                when (next) {
                    HoldDraftStatus.REVOKED -> return true
                }
                return false
            }
            HoldDraftStatus.GRANTED -> return next == HoldDraftStatus.REVOKED
            HoldDraftStatus.REVOKED -> {
                when (next) {
                    HoldDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldDraftStatus) {
        if (!canMoveTo(next)) {
            error("hold draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun netExposure(): Double = round((maximum - priority.toDouble()) * 100) / 100

    fun snapshot(): HoldDraft {
        return HoldDraft(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            sequence = sequence,
            timezone = timezone,
            priority = priority,
            externalRef = externalRef,
            windowDays = windowDays,
            label = label,
            active = active,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold draft in status requested. */
fun newHoldDraft(code: String, name: String): HoldDraft {
    return HoldDraft(
        code = code,
        name = name,
        durationMinutes = 0,
        sequence = 0,
        timezone = "",
        priority = 0,
        externalRef = "",
        windowDays = 0,
        label = "",
        active = false,
        maximum = 0.0,
        status = HoldDraftStatus.REQUESTED,
    )
}
