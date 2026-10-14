package com.acme.transport.customs.broker.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered broker schedule is in its life cycle. */
enum class TieredBrokerScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A tiered broker schedule. Its code is unique within a tenant. */
data class TieredBrokerSchedule(
    var code: String,
    var label: String,
    var windowDays: Int,
    var reviewer: String,
    var archived: Boolean,
    var minimum: Double,
    var approved: Boolean,
    var weight: Double,
    var ownerId: String,
    var priority: Int,
    var phone: String,
    var status: TieredBrokerScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBrokerScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBrokerScheduleStatus): Boolean {
        when (status) {
            TieredBrokerScheduleStatus.DRAFT -> return next == TieredBrokerScheduleStatus.PENDING || next == TieredBrokerScheduleStatus.SUSPENDED
            TieredBrokerScheduleStatus.PENDING -> return next == TieredBrokerScheduleStatus.ACTIVE || next == TieredBrokerScheduleStatus.SUSPENDED
            TieredBrokerScheduleStatus.ACTIVE -> return next == TieredBrokerScheduleStatus.SUSPENDED || next == TieredBrokerScheduleStatus.CLOSED
            TieredBrokerScheduleStatus.SUSPENDED -> {
                when (next) {
                    TieredBrokerScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredBrokerScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tiered broker schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round((priority.toDouble() - minimum) * 100) / 100

    fun snapshot(): TieredBrokerSchedule {
        return TieredBrokerSchedule(
            code = code,
            label = label,
            windowDays = windowDays,
            reviewer = reviewer,
            archived = archived,
            minimum = minimum,
            approved = approved,
            weight = weight,
            ownerId = ownerId,
            priority = priority,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered broker schedule in status draft. */
fun newTieredBrokerSchedule(code: String, label: String): TieredBrokerSchedule {
    return TieredBrokerSchedule(
        code = code,
        label = label,
        windowDays = 0,
        reviewer = "",
        archived = false,
        minimum = 0.0,
        approved = false,
        weight = 0.0,
        ownerId = "",
        priority = 0,
        phone = "",
        status = TieredBrokerScheduleStatus.DRAFT,
    )
}
