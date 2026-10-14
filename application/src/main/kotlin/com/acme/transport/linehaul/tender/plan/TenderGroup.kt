package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tender group is in its life cycle. */
enum class TenderGroupStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tender group and its current status. */
data class TenderGroup(
    var code: String,
    var title: String,
    var ceiling: Double,
    var startsAt: Int,
    var discount: Double,
    var channel: String,
    var ratio: Double,
    var priority: Int,
    var limit: Int,
    var weight: Double,
    var timezone: String,
    var status: TenderGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderGroupStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderGroupStatus): Boolean {
        when (status) {
            TenderGroupStatus.IDLE -> return next == TenderGroupStatus.WARMING || next == TenderGroupStatus.READY
            TenderGroupStatus.WARMING -> {
                when (next) {
                    TenderGroupStatus.DRAINING, TenderGroupStatus.STOPPED -> return true
                }
                return false
            }
            TenderGroupStatus.READY -> {
                when (next) {
                    TenderGroupStatus.DRAINING, TenderGroupStatus.STOPPED -> return true
                }
                return false
            }
            TenderGroupStatus.DRAINING -> return next == TenderGroupStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TenderGroupStatus) {
        if (!canMoveTo(next)) {
            error("tender group $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"tender group"} $code"

    fun adjustedExposure(): Double = round((discount - limit.toDouble()) * 100) / 100

    fun snapshot(): TenderGroup {
        return TenderGroup(
            code = code,
            title = title,
            ceiling = ceiling,
            startsAt = startsAt,
            discount = discount,
            channel = channel,
            ratio = ratio,
            priority = priority,
            limit = limit,
            weight = weight,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tender group in status idle. */
fun newTenderGroup(code: String, title: String): TenderGroup {
    return TenderGroup(
        code = code,
        title = title,
        ceiling = 0.0,
        startsAt = 0,
        discount = 0.0,
        channel = "",
        ratio = 0.0,
        priority = 0,
        limit = 0,
        weight = 0.0,
        timezone = "",
        status = TenderGroupStatus.IDLE,
    )
}
