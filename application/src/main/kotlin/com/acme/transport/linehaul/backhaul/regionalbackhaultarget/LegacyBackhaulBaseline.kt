package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy backhaul baseline is in its life cycle. */
enum class LegacyBackhaulBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One legacy backhaul baseline, with what the planners need to act on it. */
data class LegacyBackhaulBaseline(
    var code: String,
    var name: String,
    var timezone: String,
    var weight: Double,
    var score: Double,
    var archived: Boolean,
    var externalRef: String,
    var status: LegacyBackhaulBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyBackhaulBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyBackhaulBaselineStatus): Boolean {
        when (status) {
            LegacyBackhaulBaselineStatus.REQUESTED -> return next == LegacyBackhaulBaselineStatus.REVOKED || next == LegacyBackhaulBaselineStatus.EXPIRED
            LegacyBackhaulBaselineStatus.GRANTED -> return next == LegacyBackhaulBaselineStatus.EXPIRED
            LegacyBackhaulBaselineStatus.REVOKED -> {
                when (next) {
                    LegacyBackhaulBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyBackhaulBaselineStatus) {
        if (!canMoveTo(next)) {
            error("legacy backhaul baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedValue(): Double = round((score + weight) * 100) / 100

    fun snapshot(): LegacyBackhaulBaseline {
        return LegacyBackhaulBaseline(
            code = code,
            name = name,
            timezone = timezone,
            weight = weight,
            score = score,
            archived = archived,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy backhaul baseline in status requested. */
fun newLegacyBackhaulBaseline(code: String, name: String): LegacyBackhaulBaseline {
    return LegacyBackhaulBaseline(
        code = code,
        name = name,
        timezone = "",
        weight = 0.0,
        score = 0.0,
        archived = false,
        externalRef = "",
        status = LegacyBackhaulBaselineStatus.REQUESTED,
    )
}
