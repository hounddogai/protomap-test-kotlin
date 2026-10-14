package com.acme.transport.network.transittime.scheduledtimebatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary transit time window is in its life cycle. */
enum class PrimaryTransitTimeWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a primary transit time window and its current status. */
data class PrimaryTransitTimeWindow(
    var code: String,
    var title: String,
    var batchSize: Int,
    var startsAt: Int,
    var dock: String,
    var phone: String,
    var updatedAt: Int,
    var margin: Double,
    var status: PrimaryTransitTimeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTransitTimeWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTransitTimeWindowStatus): Boolean {
        when (status) {
            PrimaryTransitTimeWindowStatus.CREATED -> return next == PrimaryTransitTimeWindowStatus.PUBLISHED
            PrimaryTransitTimeWindowStatus.VALIDATED -> return next == PrimaryTransitTimeWindowStatus.PUBLISHED
            PrimaryTransitTimeWindowStatus.PUBLISHED -> {
                when (next) {
                    PrimaryTransitTimeWindowStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTransitTimeWindowStatus) {
        if (!canMoveTo(next)) {
            error("primary transit time window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netValue(): Double = round(updatedAt.toDouble() * batchSize.toDouble() * 100) / 100

    fun snapshot(): PrimaryTransitTimeWindow {
        return PrimaryTransitTimeWindow(
            code = code,
            title = title,
            batchSize = batchSize,
            startsAt = startsAt,
            dock = dock,
            phone = phone,
            updatedAt = updatedAt,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary transit time window in status created. */
fun newPrimaryTransitTimeWindow(code: String, title: String): PrimaryTransitTimeWindow {
    return PrimaryTransitTimeWindow(
        code = code,
        title = title,
        batchSize = 0,
        startsAt = 0,
        dock = "",
        phone = "",
        updatedAt = 0,
        margin = 0.0,
        status = PrimaryTransitTimeWindowStatus.CREATED,
    )
}
