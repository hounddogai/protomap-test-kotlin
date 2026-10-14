package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal locker rule is in its life cycle. */
enum class SeasonalLockerRuleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A seasonal locker rule as the locker team keeps it. */
data class SeasonalLockerRule(
    var code: String,
    var title: String,
    var version: Int,
    var segment: String,
    var limit: Int,
    var summary: String,
    var tags: MutableList<String>,
    var maximum: Double,
    var channel: String,
    var currency: String,
    var windowDays: Int,
    var status: SeasonalLockerRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalLockerRuleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalLockerRuleStatus): Boolean {
        when (status) {
            SeasonalLockerRuleStatus.NEW -> {
                when (next) {
                    SeasonalLockerRuleStatus.DONE, SeasonalLockerRuleStatus.FAILED -> return true
                }
                return false
            }
            SeasonalLockerRuleStatus.QUEUED -> {
                when (next) {
                    SeasonalLockerRuleStatus.RUNNING, SeasonalLockerRuleStatus.FAILED -> return true
                }
                return false
            }
            SeasonalLockerRuleStatus.RUNNING -> return next == SeasonalLockerRuleStatus.DONE || next == SeasonalLockerRuleStatus.FAILED
            SeasonalLockerRuleStatus.DONE -> {
                when (next) {
                    SeasonalLockerRuleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalLockerRuleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal locker rule $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((version.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): SeasonalLockerRule {
        return SeasonalLockerRule(
            code = code,
            title = title,
            version = version,
            segment = segment,
            limit = limit,
            summary = summary,
            tags = tags,
            maximum = maximum,
            channel = channel,
            currency = currency,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal locker rule in status new. */
fun newSeasonalLockerRule(code: String, title: String): SeasonalLockerRule {
    return SeasonalLockerRule(
        code = code,
        title = title,
        version = 0,
        segment = "",
        limit = 0,
        summary = "",
        tags = mutableListOf<String>(),
        maximum = 0.0,
        channel = "",
        currency = "",
        windowDays = 0,
        status = SeasonalLockerRuleStatus.NEW,
    )
}
