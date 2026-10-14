package com.acme.transport.network.transittime.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual transit time batch is in its life cycle. */
enum class ManualTransitTimeBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual transit time batch. Its code is unique within a tenant. */
data class ManualTransitTimeBatch(
    var code: String,
    var title: String,
    var cost: Double,
    var category: String,
    var maximum: Double,
    var timezone: String,
    var version: Int,
    var pinned: Boolean,
    var trailerId: String,
    var status: ManualTransitTimeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTransitTimeBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTransitTimeBatchStatus): Boolean {
        when (status) {
            ManualTransitTimeBatchStatus.IDLE -> {
                when (next) {
                    ManualTransitTimeBatchStatus.DRAINING -> return true
                }
                return false
            }
            ManualTransitTimeBatchStatus.WARMING -> {
                when (next) {
                    ManualTransitTimeBatchStatus.DRAINING, ManualTransitTimeBatchStatus.STOPPED -> return true
                }
                return false
            }
            ManualTransitTimeBatchStatus.READY -> {
                when (next) {
                    ManualTransitTimeBatchStatus.DRAINING, ManualTransitTimeBatchStatus.STOPPED -> return true
                }
                return false
            }
            ManualTransitTimeBatchStatus.DRAINING -> return next == ManualTransitTimeBatchStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ManualTransitTimeBatchStatus) {
        if (!canMoveTo(next)) {
            error("manual transit time batch $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"manual transit time batch"} $code"

    fun effectiveExposure(): Double = round((maximum - version.toDouble()) * 100) / 100

    fun snapshot(): ManualTransitTimeBatch {
        return ManualTransitTimeBatch(
            code = code,
            title = title,
            cost = cost,
            category = category,
            maximum = maximum,
            timezone = timezone,
            version = version,
            pinned = pinned,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual transit time batch in status idle. */
fun newManualTransitTimeBatch(code: String, title: String): ManualTransitTimeBatch {
    return ManualTransitTimeBatch(
        code = code,
        title = title,
        cost = 0.0,
        category = "",
        maximum = 0.0,
        timezone = "",
        version = 0,
        pinned = false,
        trailerId = "",
        status = ManualTransitTimeBatchStatus.IDLE,
    )
}
