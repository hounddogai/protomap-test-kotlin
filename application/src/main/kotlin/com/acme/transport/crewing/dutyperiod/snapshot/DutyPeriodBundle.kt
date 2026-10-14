package com.acme.transport.crewing.dutyperiod.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period bundle is in its life cycle. */
enum class DutyPeriodBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a duty period bundle and its current status. */
data class DutyPeriodBundle(
    var code: String,
    var name: String,
    var quantity: Int,
    var summary: String,
    var timezone: String,
    var sequence: Int,
    var region: String,
    var stopCount: Int,
    var createdAt: Int,
    var status: DutyPeriodBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodBundleStatus): Boolean {
        when (status) {
            DutyPeriodBundleStatus.NEW -> {
                when (next) {
                    DutyPeriodBundleStatus.FAILED -> return true
                }
                return false
            }
            DutyPeriodBundleStatus.QUEUED -> return next == DutyPeriodBundleStatus.RUNNING || next == DutyPeriodBundleStatus.FAILED
            DutyPeriodBundleStatus.RUNNING -> return next == DutyPeriodBundleStatus.FAILED
            DutyPeriodBundleStatus.DONE -> {
                when (next) {
                    DutyPeriodBundleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodBundleStatus) {
        if (!canMoveTo(next)) {
            error("duty period bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"duty period bundle"} $code"

    fun weightedTotal(): Double = round((quantity.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodBundle {
        return DutyPeriodBundle(
            code = code,
            name = name,
            quantity = quantity,
            summary = summary,
            timezone = timezone,
            sequence = sequence,
            region = region,
            stopCount = stopCount,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a duty period bundle in status new. */
fun newDutyPeriodBundle(code: String, name: String): DutyPeriodBundle {
    return DutyPeriodBundle(
        code = code,
        name = name,
        quantity = 0,
        summary = "",
        timezone = "",
        sequence = 0,
        region = "",
        stopCount = 0,
        createdAt = 0,
        status = DutyPeriodBundleStatus.NEW,
    )
}
