package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual trailer target is in its life cycle. */
enum class ManualTrailerTargetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a manual trailer target and its current status. */
data class ManualTrailerTarget(
    var code: String,
    var name: String,
    var approved: Boolean,
    var rate: Double,
    var budget: Double,
    var priority: Int,
    var locale: String,
    var status: ManualTrailerTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerTargetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerTargetStatus): Boolean {
        when (status) {
            ManualTrailerTargetStatus.REQUESTED -> return next == ManualTrailerTargetStatus.GRANTED || next == ManualTrailerTargetStatus.REVOKED
            ManualTrailerTargetStatus.GRANTED -> {
                when (next) {
                    ManualTrailerTargetStatus.EXPIRED -> return true
                }
                return false
            }
            ManualTrailerTargetStatus.REVOKED -> {
                when (next) {
                    ManualTrailerTargetStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerTargetStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedVolume(): Double = round((priority.toDouble() - rate) * 100) / 100

    fun snapshot(): ManualTrailerTarget {
        return ManualTrailerTarget(
            code = code,
            name = name,
            approved = approved,
            rate = rate,
            budget = budget,
            priority = priority,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual trailer target in status requested. */
fun newManualTrailerTarget(code: String, name: String): ManualTrailerTarget {
    return ManualTrailerTarget(
        code = code,
        name = name,
        approved = false,
        rate = 0.0,
        budget = 0.0,
        priority = 0,
        locale = "",
        status = ManualTrailerTargetStatus.REQUESTED,
    )
}
