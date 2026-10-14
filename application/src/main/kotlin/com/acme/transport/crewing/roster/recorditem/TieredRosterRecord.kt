package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered roster record records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a tiered roster record is in its life cycle. */
enum class TieredRosterRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a tiered roster record and its current status. */
data class TieredRosterRecord(
    var code: String,
    var name: String,
    var startsAt: Int,
    var maximum: Double,
    var approved: Boolean,
    var dock: String,
    var status: TieredRosterRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRosterRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRosterRecordStatus): Boolean {
        when (status) {
            TieredRosterRecordStatus.NEW -> {
                when (next) {
                    TieredRosterRecordStatus.FAILED -> return true
                }
                return false
            }
            TieredRosterRecordStatus.QUEUED -> {
                when (next) {
                    TieredRosterRecordStatus.RUNNING, TieredRosterRecordStatus.DONE -> return true
                }
                return false
            }
            TieredRosterRecordStatus.RUNNING -> {
                when (next) {
                    TieredRosterRecordStatus.DONE, TieredRosterRecordStatus.FAILED -> return true
                }
                return false
            }
            TieredRosterRecordStatus.DONE -> return next == TieredRosterRecordStatus.FAILED
        }
        return false
    }

    fun moveTo(next: TieredRosterRecordStatus) {
        if (!canMoveTo(next)) {
            error("tiered roster record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedExposure(): Double = round((maximum - startsAt.toDouble()) * 100) / 100

    fun snapshot(): TieredRosterRecord {
        return TieredRosterRecord(
            code = code,
            name = name,
            startsAt = startsAt,
            maximum = maximum,
            approved = approved,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered roster record in status new. */
fun newTieredRosterRecord(code: String, name: String): TieredRosterRecord {
    return TieredRosterRecord(
        code = code,
        name = name,
        startsAt = 0,
        maximum = 0.0,
        approved = false,
        dock = "",
        status = TieredRosterRecordStatus.NEW,
    )
}
