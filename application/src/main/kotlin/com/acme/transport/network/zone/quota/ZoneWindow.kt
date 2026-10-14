package com.acme.transport.network.zone.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone window is in its life cycle. */
enum class ZoneWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a zone window and its current status. */
data class ZoneWindow(
    var code: String,
    var title: String,
    var source: String,
    var createdAt: Int,
    var description: String,
    var rate: Double,
    var updatedAt: Int,
    var stopCount: Int,
    var weight: Double,
    var status: ZoneWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneWindowStatus): Boolean {
        when (status) {
            ZoneWindowStatus.DRAFT -> return next == ZoneWindowStatus.PENDING || next == ZoneWindowStatus.SUSPENDED
            ZoneWindowStatus.PENDING -> return next == ZoneWindowStatus.ACTIVE
            ZoneWindowStatus.ACTIVE -> return next == ZoneWindowStatus.SUSPENDED || next == ZoneWindowStatus.CLOSED
            ZoneWindowStatus.SUSPENDED -> {
                when (next) {
                    ZoneWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneWindowStatus) {
        if (!canMoveTo(next)) {
            error("zone window $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedTotal(): Double = round(rate * createdAt.toDouble() * 100) / 100

    fun snapshot(): ZoneWindow {
        return ZoneWindow(
            code = code,
            title = title,
            source = source,
            createdAt = createdAt,
            description = description,
            rate = rate,
            updatedAt = updatedAt,
            stopCount = stopCount,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a zone window in status draft. */
fun newZoneWindow(code: String, title: String): ZoneWindow {
    return ZoneWindow(
        code = code,
        title = title,
        source = "",
        createdAt = 0,
        description = "",
        rate = 0.0,
        updatedAt = 0,
        stopCount = 0,
        weight = 0.0,
        status = ZoneWindowStatus.DRAFT,
    )
}
